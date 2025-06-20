package com.enaya.product_service.infrastructure.persistence.repository;

import com.enaya.product_service.domain.model.product.ProductVariant;
import com.enaya.product_service.domain.repository.ProductVariantRepository;
import com.enaya.product_service.infrastructure.persistence.jpa.ProductVariantJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ProductVariantRepositoryImpl implements ProductVariantRepository {

    private final ProductVariantJpaRepository jpaRepository;

    @Override
    public ProductVariant save(ProductVariant variant) {
        return jpaRepository.save(variant);
    }

    @Override
    public Optional<ProductVariant> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<ProductVariant> findByProductId(UUID productId) {
        return jpaRepository.findByProductId(productId);
    }

    @Override
    public Page<ProductVariant> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsBySku(String sku) {
        return jpaRepository.existsBySku(sku);
    }

    @Override
    public List<ProductVariant> findActiveVariants() {
        return jpaRepository.findActiveVariants();
    }

    @Override
    public List<ProductVariant> findByAttributes(List<String> attributeNames, List<String> attributeValues) {
        return jpaRepository.findByAttributes(attributeNames, attributeValues);
    }

    @Override
    public List<ProductVariant> findByPriceRange(double minPrice, double maxPrice) {
        return jpaRepository.findByPriceRange(minPrice, maxPrice);
    }
} 