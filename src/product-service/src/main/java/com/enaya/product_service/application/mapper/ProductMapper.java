package com.enaya.product_service.application.mapper;

import com.enaya.product_service.application.dto.request.CreateProductRequest;
import com.enaya.product_service.application.dto.request.CreateProductVariantRequest;
import com.enaya.product_service.application.dto.request.UpdateProductRequest;
import com.enaya.product_service.application.dto.request.UpdateProductVariantRequest;
import com.enaya.product_service.application.dto.response.ProductResponse;
import com.enaya.product_service.application.dto.response.ProductVariantResponse;
import com.enaya.product_service.domain.model.product.Product;
import com.enaya.product_service.domain.model.product.ProductVariant;
import com.enaya.product_service.domain.model.collection.Collection;
import com.enaya.product_service.domain.repository.CollectionRepository;
import com.enaya.product_service.domain.model.product.valueobjects.JewelryDimensions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.UUID;

@Component
public class ProductMapper {

    @Autowired
    private CollectionRepository collectionRepository;

    public ProductResponse toResponse(Product product) {
        if (product == null) {
            return null;
        }

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .sku(product.getSku())
                .price(product.getPrice().getAmount())
                .currency(product.getPrice().getCurrency().getCurrencyCode())
                .categoryId(product.getCategoryId())
                .collectionIds(product.getCollections() != null ? product.getCollections().stream().map(Collection::getId).toList() : null)
                .attributes(product.getAttributes().stream()
                        .map(attr -> new ProductResponse.Attribute(attr.getName(), attr.getValue()))
                        .collect(Collectors.toList()))
                .images(product.getImages().stream()
                        .map(img -> new ProductResponse.Image(img.getUrl(), img.getAltText(), img.getDisplayOrder()))
                        .collect(Collectors.toList()))
                .variants(product.getVariants().stream()
                        .map(this::toVariantResponse)
                        .collect(Collectors.toList()))
                .active(product.isActive())
                .creationDate(product.getCreationDate())
                .modificationDate(product.getModificationDate())
                .version(product.getVersion())
                .build();
    }

    public ProductVariantResponse toVariantResponse(ProductVariant variant) {
        if (variant == null) {
            return null;
        }

        return ProductVariantResponse.builder()
                .id(variant.getId())
                .name(variant.getName())
                .sku(variant.getSku())
                .price(variant.getPrice().getAmount())
                .currency(variant.getPrice().getCurrency().getCurrencyCode())
                .dimensions(variant.getDimensions() != null ? new ProductVariantResponse.Dimensions(
                        variant.getDimensions().getLength(),
                        variant.getDimensions().getWidth(),
                        variant.getDimensions().getHeight(),
                        variant.getDimensions().getWeight()
                ) : null)
                .specificAttributes(variant.getSpecificAttributes().stream()
                        .map(attr -> new ProductVariantResponse.Attribute(attr.getName(), attr.getValue()))
                        .collect(Collectors.toList()))
                .images(variant.getImages().stream()
                        .map(img -> new ProductVariantResponse.Image(img.getUrl(), img.getAltText(), img.getDisplayOrder()))
                        .collect(Collectors.toList()))
                .active(variant.isActive())
                .stockStatus(variant.getStockStatus())
                .rating(variant.getRating())
                .reviewCount(variant.getReviewCount())
                .creationDate(variant.getCreationDate())
                .modificationDate(variant.getModificationDate())
                .version(variant.getVersion())
                .build();
    }

    public Product toEntity(CreateProductRequest request) {
        if (request == null) {
            return null;
        }

        return Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .sku(request.getSku())
                .price(request.getPrice())
                .categoryId(request.getCategoryId())
                .collections(request.getCollectionIds() != null ? fetchCollectionsByIds(request.getCollectionIds()) : new ArrayList<>())
                .attributes(request.getAttributes())
                .images(request.getImages())
                .active(true) // Par défaut, un nouveau produit est actif
                .build();
    }

    public ProductVariant toVariantEntity(CreateProductVariantRequest request) {
        if (request == null) {
            return null;
        }

        JewelryDimensions dimensions = null;
        if (request.getDimensions() != null) {
            dimensions = JewelryDimensions.of(
                request.getDimensions().getLength().doubleValue(),
                request.getDimensions().getWidth().doubleValue(),
                request.getDimensions().getHeight().doubleValue(),
                0.0 // Default weight, can be updated later
            );
        }

        return ProductVariant.builder()
                .name(request.getName())
                .sku(request.getSku())
                .price(request.getPrice())
                .dimensions(dimensions)
                .specificAttributes(request.getSpecificAttributes())
                .images(request.getImages())
                .active(true) // Par défaut, une nouvelle variante est active
                .rating(request.getRating())
                .reviewCount(request.getReviewCount())
                .build();
    }

    public void updateEntity(Product product, UpdateProductRequest request) {
        if (product == null || request == null) {
            return;
        }

        // Mise à jour des informations de base
        if (request.getName() != null || request.getDescription() != null) {
            product.updateBasicInfo(
                    request.getName() != null ? request.getName() : product.getName(),
                    request.getDescription() != null ? request.getDescription() : product.getDescription()
            );
        }

        // Mise à jour du prix
        if (request.getPrice() != null) {
            product.updatePrice(request.getPrice());
        }

        // Mise à jour de la catégorie
        if (request.getCategoryId() != null) {
            product.changeCategory(request.getCategoryId());
        }

        // Mise à jour des collections
        if (request.getCollectionIds() != null) {
            product.getCollections().clear();
            product.getCollections().addAll(fetchCollectionsByIds(request.getCollectionIds()));
        }

        // Mise à jour des attributs
        if (request.getAttributes() != null) {
            product.getAttributes().clear();
            product.getAttributes().addAll(request.getAttributes());
        }

        // Mise à jour des images
        if (request.getImages() != null) {
            product.getImages().clear();
            product.getImages().addAll(request.getImages());
        }

        // Mise à jour du statut actif
        if (request.getActive() != null) {
            if (request.getActive()) {
                product.activate();
            } else {
                product.deactivate();
            }
        }
    }

    public void updateVariantEntity(ProductVariant variant, UpdateProductVariantRequest request) {
        if (variant == null || request == null) {
            return;
        }

        // Mise à jour des informations de base
        if (request.getName() != null) {
            variant.updateBasicInfo(request.getName());
        }

        // Mise à jour du prix
        if (request.getPrice() != null) {
            variant.updatePrice(request.getPrice());
        }

        // Mise à jour du statut de stock
        if (request.getStockStatus() != null) {
            variant.updateStockStatus(request.getStockStatus());
        }

        // Mise à jour de la note
        if (request.getRating() != null) {
            variant.updateRating(request.getRating());
        }

        // Mise à jour du nombre de reviews
        if (request.getReviewCount() != null) {
            for (int i = 0; i < request.getReviewCount(); i++) {
                variant.incrementReviewCount();
            }
        }

        // Mise à jour des attributs spécifiques
        if (request.getSpecificAttributes() != null) {
            variant.getSpecificAttributes().clear();
            variant.getSpecificAttributes().addAll(request.getSpecificAttributes());
        }

        // Mise à jour des images
        if (request.getImages() != null) {
            variant.getImages().clear();
            variant.getImages().addAll(request.getImages());
        }

        // Mise à jour du statut actif
        if (request.getActive() != null) {
            if (request.getActive()) {
                variant.activate();
            } else {
                variant.deactivate();
            }
        }

        // Mise à jour des dimensions
        if (request.getDimensions() != null) {
            variant.updateDimensions(JewelryDimensions.of(request.getDimensions().getLength().doubleValue(), request.getDimensions().getWidth().doubleValue(), request.getDimensions().getHeight().doubleValue(), variant.getDimensions().getWeight().doubleValue()));
        }
    }

    public List<ProductResponse> toResponseList(List<Product> products) {
        if (products == null) {
            return null;
        }
        return products.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<ProductVariantResponse> toVariantResponseList(List<ProductVariant> variants) {
        if (variants == null) {
            return null;
        }
        return variants.stream()
                .map(this::toVariantResponse)
                .collect(Collectors.toList());
    }

    private List<Collection> fetchCollectionsByIds(List<UUID> ids) {
        return collectionRepository.findAllById(ids);
    }
}