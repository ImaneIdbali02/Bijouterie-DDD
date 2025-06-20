package com.enaya.product_service.infrastructure.persistence.jpa;

import com.enaya.product_service.domain.model.collection.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;

public interface CollectionJpaRepository extends JpaRepository<Collection, UUID> {

    @Query("SELECT c FROM Collection c WHERE c.active = true")
    List<Collection> findActiveCollections();

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Collection c WHERE c.name = :name")
    boolean existsByName(@Param("name") String name);

    @Query("SELECT c FROM Collection c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Collection> findByNameContaining(@Param("name") String name);

    @Query("SELECT c FROM Collection c JOIN c.productIds p WHERE p = :productId")
    List<Collection> findByProductId(@Param("productId") UUID productId);
} 