package com.enaya.product_service.domain.repository;

import com.enaya.product_service.domain.model.product.Product;
import com.enaya.product_service.domain.model.product.ProductVariant;
import com.enaya.product_service.domain.model.product.valueobjects.*;
import co.elastic.clients.elasticsearch._types.SortOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface ProductRepository {
    // Méthodes de base
    Product save(Product product);
    Optional<Product> findById(UUID id);
    Optional<Product> findById(String id);

    @Transactional(readOnly = true)
    Page<Product> findAll(Pageable pageable);

    void deleteById(UUID id);
    void deleteById(String id);
    boolean existsById(UUID id);
    boolean existsById(String id);
    void deleteProduct(UUID id);
    
    // Nouvelles méthodes Iterable/CRUD
    Iterable<Product> findAll(Sort sort);
    <S extends Product> Iterable<S> saveAll(Iterable<S> products);
    Iterable<Product> findAll();
    Iterable<Product> findAllById(Iterable<String> ids);
    long count();
    void delete(Product entity);
    void deleteAllById(Iterable<? extends String> ids);
    void deleteAll(Iterable<? extends Product> entities);
    void deleteAll();
    
    // Méthodes de recherche basiques
    List<Product> findByCategoryId(UUID categoryId);
    List<Product> findByCollectionId(UUID collectionId);
    List<Product> findByAttributes(List<String> attributeNames, List<String> attributeValues);
    List<Product> findByAttributes(Map<String, String> attributes);
    List<Product> findByAttributesValue(String value);
    List<Product> findByVariantsSku(String sku);
    List<Product> findByVariantsDimensions(JewelryDimensions dimensions);
    List<Product> findByVariantsPriceBetween(Price minPrice, Price maxPrice);
    List<Product> findByVariantsStockStatus(ProductVariant.StockStatus status);
    List<Product> findByVariantsPriceLessThanOriginalPrice();
    List<Product> findByPriceRange(double minPrice, double maxPrice);
    List<Product> findByNameContaining(String name);
    List<Product> findByValidityPeriodContaining(LocalDateTime date);
    List<Product> findByCreatedAtAfter(LocalDateTime since);
    List<Product> findByOrderByViewCountDesc();
    List<Product> findActiveProducts();
    List<Product> findRelatedProducts(UUID categoryId, Set<ProductAttribute> attributes, UUID excludeProductId, int limit);

    List<Product> searchProductsWithFilters(String query, Map<String, Object> filters,
                                            String sortField, SortOrder sortOrder,
                                            int page, int size);
    List<Product> findSimilarProducts(UUID productId, int size);
    List<Product> findByScoreGreaterThan(String query, float minScore);
    List<Product> findBySuggestions(String query);
    List<Product> findByWithCategoryAggregation(String query);
    List<Product> findByWithPriceAggregation(String query);
    List<Product> findByWithAttributesAggregation(String query);

    // Méthodes de recherche avancée
    Page<Product> searchProducts(
            String query,
            UUID categoryId,
            UUID collectionId,
            Price minPrice,
            Price maxPrice,
            Set<ProductAttribute> attributes,
            boolean inStock,
            LocalDateTime date,
            Pageable pageable);
    
    // Méthodes paginées
    Page<Product> findByCategoryId(UUID categoryId, Pageable pageable);
    Page<Product> findByCollectionId(UUID collectionId, Pageable pageable);
    Page<Product> findActiveProducts(Pageable pageable);
    Page<Product> findByAttributes(List<String> attributeNames, List<String> attributeValues, Pageable pageable);
    Page<Product> findByAttributes(Map<String, String> attributes, Pageable pageable);
    Page<Product> findByAttributesValue(String value, Pageable pageable);
    Page<Product> findByVariantsSku(String sku, Pageable pageable);
    Page<Product> findByVariantsDimensions(JewelryDimensions dimensions, Pageable pageable);
    Page<Product> findByVariantsPriceBetween(Price minPrice, Price maxPrice, Pageable pageable);
    Page<Product> findByVariantsStockStatus(ProductVariant.StockStatus status, Pageable pageable);
    Page<Product> findByVariantsPriceLessThanOriginalPrice(Pageable pageable);
    Page<Product> findByPriceRange(double minPrice, double maxPrice, Pageable pageable);
    Page<Product> findByNameContaining(String name, Pageable pageable);
    Page<Product> findByValidityPeriodContaining(LocalDateTime date, Pageable pageable);
    Page<Product> findByCreatedAtAfter(LocalDateTime since, Pageable pageable);
    Page<Product> findByOrderByViewCountDesc(Pageable pageable);
    
    // Vérifications
    boolean existsBySku(String sku);

    Page<Product> findByActiveTrue(Pageable pageable);
    Page<Product> searchSimilar(Product product, String[] fields, Pageable pageable);
} 