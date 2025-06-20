package com.enaya.product_service.infrastructure.persistence.jpa;

import com.enaya.product_service.domain.model.product.Product;
import com.enaya.product_service.domain.model.product.ProductVariant;
import com.enaya.product_service.domain.model.product.valueobjects.JewelryDimensions;
import com.enaya.product_service.domain.model.product.valueobjects.ProductAttribute;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.HashSet;
import java.util.Optional;

public interface ProductJpaRepository extends JpaRepository<Product, UUID> {

    // Méthodes de base avec indexation
    @QueryHints(@QueryHint(name = "org.hibernate.cacheable", value = "true"))
    List<Product> findByCategoryId(UUID categoryId);
    
    @QueryHints(@QueryHint(name = "org.hibernate.cacheable", value = "true"))
    Page<Product> findByCategoryId(UUID categoryId, Pageable pageable);

    @Query("SELECT DISTINCT p FROM Product p JOIN p.collections c WHERE c.id = :collectionId")
    @QueryHints(@QueryHint(name = "org.hibernate.cacheable", value = "true"))
    List<Product> findByCollectionId(@Param("collectionId") UUID collectionId);
    
    @Query("SELECT DISTINCT p FROM Product p JOIN p.collections c WHERE c.id = :collectionId")
    @QueryHints(@QueryHint(name = "org.hibernate.cacheable", value = "true"))
    Page<Product> findByCollectionId(@Param("collectionId") UUID collectionId, Pageable pageable);

    // Méthodes avec verrouillage optimiste
    @Lock(LockModeType.OPTIMISTIC)
    List<Product> findByActiveTrue();
    
    @Lock(LockModeType.OPTIMISTIC)
    Page<Product> findByActiveTrue(Pageable pageable);

    // Méthodes avec requêtes JPQL optimisées
    @Query("SELECT p FROM Product p WHERE p.creationDate > :since AND p.active = true")
    List<Product> findByCreationDateAfter(@Param("since") LocalDateTime since);

    @Query("SELECT p FROM Product p WHERE p.creationDate > :since AND p.active = true")
    Page<Product> findByCreationDateAfter(@Param("since") LocalDateTime since, Pageable pageable);

    @Query("SELECT p FROM Product p ORDER BY p.modificationDate DESC")
    List<Product> findAllByOrderByModificationDateDesc();
    
    @Query("SELECT p FROM Product p ORDER BY p.modificationDate DESC")
    Page<Product> findAllByOrderByModificationDateDesc(Pageable pageable);

    // Recherche full-text avec indexation
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Product> findByNameValueContainingIgnoreCase(@Param("name") String name);
    
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Product> findByNameValueContainingIgnoreCase(@Param("name") String name, Pageable pageable);

    // Méthodes de prix avec BigDecimal (suppression de la version double)
    @Query("SELECT p FROM Product p WHERE p.price.amount BETWEEN :minPrice AND :maxPrice")
    List<Product> findByPriceAmountBetween(
        @Param("minPrice") BigDecimal minPrice, 
        @Param("maxPrice") BigDecimal maxPrice
    );
    
    @Query("SELECT p FROM Product p WHERE p.price.amount BETWEEN :minPrice AND :maxPrice")
    Page<Product> findByPriceAmountBetween(
        @Param("minPrice") BigDecimal minPrice, 
        @Param("maxPrice") BigDecimal maxPrice, 
        Pageable pageable
    );

    // Vérification d'existence avec index
    @QueryHints(@QueryHint(name = "org.hibernate.cacheable", value = "true"))
    boolean existsBySku(String sku);

    // Méthodes d'attributs avec requêtes optimisées
    @Query("SELECT DISTINCT p FROM Product p JOIN p.attributes a " +
           "WHERE a.name IN :names AND a.value IN :values")
    List<Product> findByAttributesNameInAndAttributesValueIn(
        @Param("names") List<String> names, 
        @Param("values") List<String> values
    );
    
    @Query("SELECT DISTINCT p FROM Product p JOIN p.attributes a " +
           "WHERE a.name IN :names AND a.value IN :values")
    Page<Product> findByAttributesNameInAndAttributesValueIn(
        @Param("names") List<String> names, 
        @Param("values") List<String> values, 
        Pageable pageable
    );

    // Recherche par map d'attributs avec requête dynamique
    @Query("SELECT DISTINCT p FROM Product p JOIN p.attributes a " +
           "WHERE a.name IN :names AND a.value IN :values")
    List<Product> findByAttributesMap(
        @Param("names") Set<String> names,
        @Param("values") Set<String> values
    );
    
    @Query("SELECT DISTINCT p FROM Product p JOIN p.attributes a " +
           "WHERE a.name IN :names AND a.value IN :values")
    Page<Product> findByAttributesMap(
        @Param("names") Set<String> names,
        @Param("values") Set<String> values,
        Pageable pageable
    );

    // Méthode utilitaire pour convertir la Map en paramètres
    default List<Product> findByAttributesMap(Map<String, String> attributes) {
        return findByAttributesMap(
            new HashSet<>(attributes.keySet()),
            new HashSet<>(attributes.values())
        );
    }

    default Page<Product> findByAttributesMap(Map<String, String> attributes, Pageable pageable) {
        return findByAttributesMap(
            new HashSet<>(attributes.keySet()),
            new HashSet<>(attributes.values()),
            pageable
        );
    }

    // Recherche par valeur d'attribut avec index
    @Query("SELECT DISTINCT p FROM Product p JOIN p.attributes a WHERE a.value LIKE %:value%")
    List<Product> findByAttributesValueContaining(@Param("value") String value);
    
    @Query("SELECT DISTINCT p FROM Product p JOIN p.attributes a WHERE a.value LIKE %:value%")
    Page<Product> findByAttributesValueContaining(
        @Param("value") String value, 
        Pageable pageable
    );

    // Méthodes de variantes avec jointures optimisées
    @Query("SELECT DISTINCT p FROM Product p JOIN p.variants v WHERE v.sku = :sku")
    List<Product> findByVariantsSkuValue(@Param("sku") String sku);
    
    @Query("SELECT DISTINCT p FROM Product p JOIN p.variants v WHERE v.sku = :sku")
    Page<Product> findByVariantsSkuValue(@Param("sku") String sku, Pageable pageable);

    @Query("SELECT DISTINCT p FROM Product p JOIN p.variants v " +
           "WHERE v.dimensions = :dimensions")
    List<Product> findByVariantsDimensions(@Param("dimensions") JewelryDimensions dimensions);
    
    @Query("SELECT DISTINCT p FROM Product p JOIN p.variants v " +
           "WHERE v.dimensions = :dimensions")
    Page<Product> findByVariantsDimensions(
        @Param("dimensions") JewelryDimensions dimensions, 
        Pageable pageable
    );

    // Méthodes de prix des variantes
    @Query("SELECT DISTINCT p FROM Product p JOIN p.variants v " +
           "WHERE v.price.amount BETWEEN :minPrice AND :maxPrice")
    List<Product> findByVariantsPriceAmountBetween(
        @Param("minPrice") BigDecimal minPrice, 
        @Param("maxPrice") BigDecimal maxPrice
    );
    
    @Query("SELECT DISTINCT p FROM Product p JOIN p.variants v " +
           "WHERE v.price.amount BETWEEN :minPrice AND :maxPrice")
    Page<Product> findByVariantsPriceAmountBetween(
        @Param("minPrice") BigDecimal minPrice, 
        @Param("maxPrice") BigDecimal maxPrice, 
        Pageable pageable
    );

    // Méthodes de gestion du stock
    @Query("SELECT DISTINCT p FROM Product p JOIN p.variants v " +
           "WHERE v.stockStatus = :status")
    List<Product> findByVariantsStockStatus(@Param("status") ProductVariant.StockStatus status);
    
    @Query("SELECT DISTINCT p FROM Product p JOIN p.variants v " +
           "WHERE v.stockStatus = :status")
    Page<Product> findByVariantsStockStatus(
        @Param("status") ProductVariant.StockStatus status, 
        Pageable pageable
    );

    // Méthode pour trouver les produits en stock
    @Query("SELECT DISTINCT p FROM Product p JOIN p.variants v " +
           "WHERE v.stockStatus = 'IN_STOCK'")
    List<Product> findInStockProducts();
    
    @Query("SELECT DISTINCT p FROM Product p JOIN p.variants v " +
           "WHERE v.stockStatus = 'IN_STOCK'")
    Page<Product> findInStockProducts(Pageable pageable);

    // Méthode pour trouver les produits en rupture de stock
    @Query("SELECT DISTINCT p FROM Product p JOIN p.variants v " +
           "WHERE v.stockStatus = 'OUT_OF_STOCK'")
    List<Product> findOutOfStockProducts();
    
    @Query("SELECT DISTINCT p FROM Product p JOIN p.variants v " +
           "WHERE v.stockStatus = 'OUT_OF_STOCK'")
    Page<Product> findOutOfStockProducts(Pageable pageable);

    // Méthode pour trouver les produits avec un statut de stock spécifique pour une variante
    @Query("SELECT DISTINCT p FROM Product p JOIN p.variants v " +
           "WHERE p.id = :productId AND v.id = :variantId AND v.stockStatus = :status")
    Optional<Product> findProductWithVariantStockStatus(
        @Param("productId") UUID productId,
        @Param("variantId") UUID variantId,
        @Param("status") ProductVariant.StockStatus status
    );

    // Méthode pour mettre à jour le statut de stock d'une variante
    @Modifying
    @Query("UPDATE ProductVariant v SET v.stockStatus = :status, v.modificationDate = CURRENT_TIMESTAMP, v.version = v.version + 1 " +
           "WHERE v.id = :variantId AND v.product.id = :productId")
    int updateVariantStockStatus(
        @Param("productId") UUID productId,
        @Param("variantId") UUID variantId,
        @Param("status") ProductVariant.StockStatus status
    );

    // Méthode pour mettre à jour le statut de stock de toutes les variantes d'un produit
    @Modifying
    @Query("UPDATE ProductVariant v SET v.stockStatus = :status, v.modificationDate = CURRENT_TIMESTAMP, v.version = v.version + 1 " +
           "WHERE v.product.id = :productId")
    int updateAllVariantsStockStatus(
        @Param("productId") UUID productId,
        @Param("status") ProductVariant.StockStatus status
    );

    // Méthode pour mettre à jour le statut de stock de plusieurs variantes
    @Modifying
    @Query("UPDATE ProductVariant v SET v.stockStatus = :status, v.modificationDate = CURRENT_TIMESTAMP, v.version = v.version + 1 " +
           "WHERE v.id IN :variantIds AND v.product.id = :productId")
    int updateVariantsStockStatus(
        @Param("productId") UUID productId,
        @Param("variantIds") List<UUID> variantIds,
        @Param("status") ProductVariant.StockStatus status
    );

    // Méthode pour trouver les variantes d'un produit avec un statut de stock spécifique
    @Query("SELECT v FROM ProductVariant v " +
           "WHERE v.product.id = :productId AND v.stockStatus = :status")
    List<ProductVariant> findVariantsByStockStatus(
        @Param("productId") UUID productId,
        @Param("status") ProductVariant.StockStatus status
    );

    // Méthode pour vérifier si un produit a des variantes en stock
    @Query("SELECT CASE WHEN COUNT(v) > 0 THEN true ELSE false END " +
           "FROM ProductVariant v " +
           "WHERE v.product.id = :productId AND v.stockStatus = 'IN_STOCK'")
    boolean hasVariantsInStock(@Param("productId") UUID productId);

    // Méthodes de prix réduit
    @Query("SELECT DISTINCT p FROM Product p JOIN p.variants v " +
           "WHERE v.price.amount < v.price.amount * 1.0")
    List<Product> findByVariantsPriceLessThanBasePrice();
    
    @Query("SELECT DISTINCT p FROM Product p JOIN p.variants v " +
           "WHERE v.price.amount < v.price.amount * 1.0")
    Page<Product> findByVariantsPriceLessThanBasePrice(Pageable pageable);

    // Méthodes de période de validité
    @Query("SELECT p FROM Product p " +
           "WHERE p.creationDate <= :end AND p.modificationDate >= :start")
    List<Product> findByCreationDateLessThanEqualAndModificationDateGreaterThanEqual(
        @Param("start") LocalDateTime start, 
        @Param("end") LocalDateTime end
    );
    
    @Query("SELECT p FROM Product p " +
           "WHERE p.creationDate <= :end AND p.modificationDate >= :start")
    Page<Product> findByCreationDateLessThanEqualAndModificationDateGreaterThanEqual(
        @Param("start") LocalDateTime start, 
        @Param("end") LocalDateTime end, 
        Pageable pageable
    );

    // Méthode de produits similaires optimisée
    @Query("SELECT DISTINCT p FROM Product p " +
           "WHERE p.categoryId = :categoryId " +
           "AND p.id != :excludeProductId " +
           "AND EXISTS (SELECT 1 FROM p.attributes a WHERE a IN :attributes) " +
           "ORDER BY p.modificationDate DESC")
    List<Product> findRelatedProducts(
        @Param("categoryId") UUID categoryId,
        @Param("attributes") Set<ProductAttribute> attributes,
        @Param("excludeProductId") UUID excludeProductId,
        @Param("limit") int limit
    );


}