package com.enaya.product_service.domain.service;

import com.enaya.product_service.domain.model.product.Product;
import com.enaya.product_service.domain.model.product.ProductVariant;
import com.enaya.product_service.domain.model.product.valueobjects.*;
import com.enaya.product_service.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.HashSet;

@Service
public class ProductSearchService {

    private final ProductRepository productRepository;

    public ProductSearchService(@Lazy @Qualifier("productRepositoryImpl") ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public Page<Product> searchProducts(
            String query,
            UUID categoryId,
            UUID collectionId,
            Price minPrice,
            Price maxPrice,
            Set<ProductAttribute> attributes,
            boolean inStock,
            LocalDateTime date,
            Pageable pageable) {
        
        return productRepository.searchProducts(
            query,
            categoryId,
            collectionId,
            minPrice,
            maxPrice,
            attributes,
            inStock,
            date,
            pageable
        );
    }

    @Transactional(readOnly = true)
    public Page<Product> findBySku(String sku, Pageable pageable) {
        return productRepository.findByVariantsSku(sku, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Product> findByDimensions(JewelryDimensions dimensions, Pageable pageable) {
        return productRepository.findByVariantsDimensions(dimensions, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Product> findByMaterial(String material, Pageable pageable) {
        return productRepository.findByAttributesValue(material, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Product> findByPriceRange(Price minPrice, Price maxPrice, Pageable pageable) {
        return productRepository.findByVariantsPriceBetween(minPrice, maxPrice, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Product> findByValidityPeriod(LocalDateTime date, Pageable pageable) {
        return productRepository.findByValidityPeriodContaining(date, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Product> findByAttributes(Map<String, String> attributes, Pageable pageable) {
        return productRepository.findByAttributes(attributes, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Product> findInStock(Pageable pageable) {
        return productRepository.findByVariantsStockStatus(ProductVariant.StockStatus.IN_STOCK, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Product> findOnSale(Pageable pageable) {
        return productRepository.findByVariantsPriceLessThanOriginalPrice(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Product> findNewArrivals(LocalDateTime since, Pageable pageable) {
        return productRepository.findByCreatedAtAfter(since, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Product> findPopular(Pageable pageable) {
        return productRepository.findByOrderByViewCountDesc(pageable);
    }

    @Transactional(readOnly = true)
    public List<Product> findRelated(Product product, int limit) {
        return productRepository.findRelatedProducts(
            product.getCategoryId(),
            new HashSet<>(product.getAttributes()),
            product.getId(),
            limit
        );
    }

    @Transactional(readOnly = true)
    public Page<Product> findActiveProducts(Pageable pageable) {
        return productRepository.findByActiveTrue(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Product> findProductsByCategory(UUID categoryId, Pageable pageable) {
        return productRepository.findByCategoryId(categoryId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Product> findProductsByCollection(UUID collectionId, Pageable pageable) {
        return productRepository.findByCollectionId(collectionId, pageable);
    }
}
