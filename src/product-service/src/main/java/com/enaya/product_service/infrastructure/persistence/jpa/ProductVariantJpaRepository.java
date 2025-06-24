package com.enaya.product_service.infrastructure.persistence.jpa;

import com.enaya.product_service.domain.model.product.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductVariantJpaRepository extends JpaRepository<ProductVariant, UUID> {

    @Query("SELECT v FROM ProductVariant v WHERE v.product.id = :productId")
    List<ProductVariant> findByProductId(@Param("productId") UUID productId);

    @Query("SELECT CASE WHEN COUNT(v) > 0 THEN true ELSE false END FROM ProductVariant v WHERE v.sku = :sku")
    boolean existsBySku(@Param("sku") String sku);

    @Query("SELECT v FROM ProductVariant v WHERE v.active = true")
    List<ProductVariant> findActiveVariants();

    @Query("SELECT DISTINCT v FROM ProductVariant v JOIN v.specificAttributes a WHERE a.name IN :attributeNames AND a.value IN :attributeValues")
    List<ProductVariant> findByAttributes(@Param("attributeNames") List<String> attributeNames,
                                        @Param("attributeValues") List<String> attributeValues);

    @Query("SELECT v FROM ProductVariant v WHERE v.price.amount BETWEEN :minPrice AND :maxPrice")
    List<ProductVariant> findByPriceRange(@Param("minPrice") double minPrice, @Param("maxPrice") double maxPrice);
} 