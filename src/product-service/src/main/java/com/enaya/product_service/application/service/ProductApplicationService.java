package com.enaya.product_service.application.service;

import com.enaya.product_service.application.dto.request.CreateProductRequest;
import com.enaya.product_service.application.dto.request.UpdateProductRequest;
import com.enaya.product_service.application.dto.response.ProductResponse;
import com.enaya.product_service.application.event.ProductEventPublisher;
import com.enaya.product_service.application.mapper.ProductMapper;
import com.enaya.product_service.domain.event.external.ProductOutOfStockEvent;
import com.enaya.product_service.domain.model.product.Product;
import com.enaya.product_service.domain.model.product.ProductVariant;
import com.enaya.product_service.domain.model.product.valueobjects.*;
import com.enaya.product_service.domain.repository.ProductRepository;
import com.enaya.product_service.domain.service.ProductDomainService;
import com.enaya.product_service.domain.model.collection.Collection;
import com.enaya.product_service.domain.repository.CollectionRepository;
import com.enaya.product_service.domain.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Currency;
import java.util.List;
import java.util.UUID;
import java.util.ArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductApplicationService {

    @Qualifier("productRepositoryImpl")
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductDomainService productDomainService;
    private final ProductMapper productMapper;
    private final ProductEventPublisher eventPublisher;
    @Autowired
    private CollectionRepository collectionRepository;

    @Transactional
    public void handleOutOfStock(UUID productId, UUID variantId) {
        log.info("Handling out of stock event - product: {}, variant: {}", productId, variantId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        ProductVariant variant = product.getVariants().stream()
                .filter(v -> v.getId().equals(variantId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Variant not found"));

        // Mise à jour du statut à OUT_OF_STOCK
        productDomainService.updateVariantStockStatus(variant, ProductVariant.StockStatus.OUT_OF_STOCK);

        // Sauvegarde des modifications
        productRepository.save(product);

        log.info("Product marked as out of stock - product: {}, variant: {}", productId, variantId);
    }

    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        // Valider le produit avant création
        productDomainService.validateProductCreation(
                request.getName(), 
                request.getSku(), 
                request.getPrice(), 
                request.getCategoryId()
        );

        // Vérifier que la catégorie existe
        categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category with id " + request.getCategoryId() + " not found"));

        // Créer le produit de base (Hibernate générera l'ID automatiquement)
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .sku(request.getSku())
                .price(request.getPrice())
                .categoryId(request.getCategoryId())
                .collections(request.getCollectionIds() != null ? fetchCollectionsByIds(request.getCollectionIds()) : new ArrayList<>())
                .attributes(request.getAttributes() != null ? new ArrayList<>(request.getAttributes()) : new ArrayList<>())
                .images(request.getImages() != null ? new ArrayList<>(request.getImages()) : new ArrayList<>())
                .active(request.isActive())
                .build();

        // Créer les variantes si spécifiées
        if (request.getVariants() != null) {
            request.getVariants().forEach(variantRequest -> {
                JewelryDimensions dimensions = null;
                if (variantRequest.getDimensions() != null) {
                    dimensions = JewelryDimensions.of(
                        variantRequest.getDimensions().getLength().doubleValue(),
                        variantRequest.getDimensions().getWidth().doubleValue(),
                        variantRequest.getDimensions().getHeight().doubleValue(),
                        0.0 // Default weight, can be updated later
                    );
                }

                ProductVariant variant = ProductVariant.builder()
                        .product(product)
                        .name(variantRequest.getName())
                        .sku(variantRequest.getSku())
                        .price(variantRequest.getPrice())
                        .dimensions(dimensions)
                        .specificAttributes(variantRequest.getSpecificAttributes() != null ? 
                            new ArrayList<>(variantRequest.getSpecificAttributes()) : new ArrayList<>())
                        .images(variantRequest.getImages() != null ? 
                            new ArrayList<>(variantRequest.getImages()) : new ArrayList<>())
                        .active(variantRequest.isActive())
                        .rating(variantRequest.getRating())
                        .reviewCount(variantRequest.getReviewCount())
                        .build();
                
                product.addVariant(variant);
            });
        }

        // Sauvegarder le produit une seule fois avec toutes ses variantes
        Product savedProduct = productRepository.save(product);

        // Publication de l'événement ProductCreated
        eventPublisher.publishProductCreated(savedProduct);

        return productMapper.toResponse(savedProduct);
    }

    @Transactional
    public ProductResponse updateProduct(UUID productId, UpdateProductRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        // Sauvegarde de l'ancien prix pour détecter les changements
        Price oldPrice = product.getPrice();

        // Mise à jour des informations de base
        if (request.getName() != null || request.getDescription() != null) {
            productDomainService.updateProductBasicInfo(
                    product,
                    request.getName() != null ? request.getName() : product.getName(),
                    request.getDescription() != null ? request.getDescription() : product.getDescription()
            );
        }

        // Mise à jour du prix si nécessaire
        if (request.getPrice() != null) {
            productDomainService.updateProductPrice(product, request.getPrice());

            // Publication de l'événement ProductPriceChanged si le prix a changé
            if (!oldPrice.equals(request.getPrice())) {
                eventPublisher.publishProductPriceChanged(productId, oldPrice, request.getPrice());
            }
        }

        // Mise à jour du statut actif
        if (request.getActive() != null) {
            if (request.getActive()) {
                productDomainService.activateProduct(product);
            } else {
                productDomainService.deactivateProduct(product);
            }
        }

        // Sauvegarde des modifications
        product = productRepository.save(product);

        // Publication de l'événement ProductUpdated
        eventPublisher.publishProductUpdated(product);

        return productMapper.toResponse(product);
    }

    @Transactional
    public void deleteProduct(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        // Vérification si le produit peut être supprimé
        if (!productDomainService.canDeleteProduct(product)) {
            throw new IllegalStateException("Cannot delete product with active variants");
        }

        // Suppression du produit
        productRepository.deleteById(productId);

        // Publication de l'événement ProductDeleted
        eventPublisher.publishProductDeleted(productId);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProduct(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        return productMapper.toResponse(product);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = productRepository.findAll(pageable);
        return products.map(productMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getActiveProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = productRepository.findActiveProducts(pageable);
        return products.map(productMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getProductsByCategory(UUID categoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = productRepository.findByCategoryId(categoryId, pageable);
        return products.map(productMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getProductsByCollection(UUID collectionId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = productRepository.findByCollectionId(collectionId, pageable);
        return products.map(productMapper::toResponse);
    }

    private List<Collection> fetchCollectionsByIds(List<UUID> ids) {
        return collectionRepository.findAllById(ids);
    }
}
