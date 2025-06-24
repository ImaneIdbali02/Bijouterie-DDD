package com.enaya.product_service.domain.repository;

import com.enaya.product_service.domain.model.product.ProductVariant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductVariantRepository {
    
    ProductVariant save(ProductVariant variant);
    
    Optional<ProductVariant> findById(UUID id);
    
    List<ProductVariant> findByProductId(UUID productId);
    
    Page<ProductVariant> findAll(Pageable pageable);
    
    void deleteById(UUID id);
    
    boolean existsBySku(String sku);
    
    List<ProductVariant> findActiveVariants();
    
    List<ProductVariant> findByAttributes(List<String> attributeNames, List<String> attributeValues);
    
    List<ProductVariant> findByPriceRange(double minPrice, double maxPrice);
} 