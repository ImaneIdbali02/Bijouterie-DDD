package com.enaya.product_service.infrastructure.persistence.elasticsearch.repository;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.enaya.product_service.domain.model.product.Product;
import com.enaya.product_service.domain.model.product.ProductVariant;
import com.enaya.product_service.domain.model.product.valueobjects.*;
import com.enaya.product_service.domain.repository.ProductRepository;
import com.enaya.product_service.infrastructure.persistence.elasticsearch.service.ElasticsearchSyncService;
import com.enaya.product_service.infrastructure.persistence.jpa.ProductJpaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Repository
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductRepositoryImpl implements ProductRepository {

    private final ProductJpaRepository jpaRepository;
    private final ElasticsearchClient elasticsearchClient;
    private final ElasticsearchSyncService elasticsearchSyncService;
    private final ObjectMapper objectMapper;

    private static final String PRODUCTS_INDEX = "products";
    private static final int MAX_FROM_SIZE = 10000; // Limite pour la pagination from/size

    // Méthodes de base avec cache et retry
    @Override
    @CacheEvict(value = "products", allEntries = true)
    public Product save(Product product) {
        Assert.notNull(product, "Product must not be null");
        
        // Si le produit a déjà un ID, c'est une mise à jour
        if (product.getId() != null) {
            Product savedProduct = jpaRepository.save(product);
            syncWithElasticsearch(savedProduct);
            return savedProduct;
        } else {
            // Si c'est un nouveau produit, on le sauvegarde directement
            Product savedProduct = jpaRepository.saveAndFlush(product);
            syncWithElasticsearch(savedProduct);
            return savedProduct;
        }
    }

    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000))
    protected void syncWithElasticsearch(Product product) {
        try {
            elasticsearchSyncService.indexProduct(product);
        } catch (Exception e) {
            log.error("Failed to sync product {} with Elasticsearch", product.getId(), e);
            throw e;
        }
    }

    @Override
    @Cacheable(value = "products", key = "#id")
    public Optional<Product> findById(UUID id) {
        Assert.notNull(id, "ID must not be null");
        return jpaRepository.findById(id);
    }

    @Override
    @Cacheable(value = "products", key = "#id")
    public Optional<Product> findById(String id) {
        Assert.hasText(id, "ID must not be empty");
        try {
            UUID uuid = UUID.fromString(id);
            return findById(uuid);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid UUID format: {}", id);
            return Optional.empty();
        }
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "'all_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<Product> findAll(Pageable pageable) {
        Assert.notNull(pageable, "Pageable must not be null");
        return jpaRepository.findAll(pageable);
    }

    @Override
    @CacheEvict(value = "products", allEntries = true)
    public void deleteById(UUID id) {
        Assert.notNull(id, "ID must not be null");
        jpaRepository.deleteById(id);
        elasticsearchSyncService.deleteProduct(id.toString());
    }

    @Override
    public void deleteById(String id) {
        try {
            UUID uuid = UUID.fromString(id);
            deleteById(uuid);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid UUID format for deletion: {}", id);
        }
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public boolean existsById(String id) {
        try {
            UUID uuid = UUID.fromString(id);
            return existsById(uuid);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public void deleteProduct(UUID id) {
        deleteById(id);
    }

    // Méthodes Iterable/CRUD
    @Override
    public Iterable<Product> findAll(Sort sort) {
        return jpaRepository.findAll(sort);
    }

    @Override
    public <S extends Product> Iterable<S> saveAll(Iterable<S> products) {
        List<S> savedProducts = new ArrayList<>();
        products.forEach(product -> {
            S saved = (S) save(product);
            savedProducts.add(saved);
        });
        return savedProducts;
    }

    @Override
    public Iterable<Product> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public Iterable<Product> findAllById(Iterable<String> ids) {
        List<UUID> uuidIds = StreamSupport.stream(ids.spliterator(), false)
                .map(id -> {
                    try {
                        return UUID.fromString(id);
                    } catch (IllegalArgumentException e) {
                        log.warn("Invalid UUID format: {}", id);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        return jpaRepository.findAllById(uuidIds);
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }

    @Override
    public void delete(Product entity) {
        jpaRepository.delete(entity);
        if (entity.getId() != null) {
            elasticsearchSyncService.deleteProduct(entity.getId().toString());
        }
    }

    @Override
    public void deleteAllById(Iterable<? extends String> ids) {
        ids.forEach(this::deleteById);
    }

    @Override
    public void deleteAll(Iterable<? extends Product> entities) {
        entities.forEach(this::delete);
    }

    @Override
    public void deleteAll() {
        jpaRepository.deleteAll();
        // Note: Pour une suppression complète d'index, vous pourriez vouloir recréer l'index
    }

    // Méthodes de recherche basiques
    @Override
    public List<Product> findByCategoryId(UUID categoryId) {
        return jpaRepository.findByCategoryId(categoryId);
    }

    @Override
    public List<Product> findByCollectionId(UUID collectionId) {
        return jpaRepository.findByCollectionIdsContaining(collectionId);
    }

    @Override
    public List<Product> findByAttributes(List<String> attributeNames, List<String> attributeValues) {
        return jpaRepository.findByAttributesNameInAndAttributesValueIn(attributeNames, attributeValues);
    }

    @Override
    public List<Product> findByAttributes(Map<String, String> attributes) {
        return jpaRepository.findByAttributesMap(attributes);
    }

    @Override
    public List<Product> findByAttributesValue(String value) {
        return jpaRepository.findByAttributesValueContaining(value);
    }

    @Override
    public List<Product> findByVariantsSku(String sku) {
        return jpaRepository.findByVariantsSkuValue(sku);
    }

    @Override
    public List<Product> findByVariantsDimensions(JewelryDimensions dimensions) {
        return jpaRepository.findByVariantsDimensions(dimensions);
    }

    @Override
    public List<Product> findByVariantsPriceBetween(Price minPrice, Price maxPrice) {
        return jpaRepository.findByVariantsPriceAmountBetween(
                minPrice.getAmount(), maxPrice.getAmount());
    }

    @Override
    public List<Product> findByVariantsStockStatus(ProductVariant.StockStatus status) {
        return jpaRepository.findByVariantsStockStatus(status);
    }

    @Override
    public List<Product> findByVariantsPriceLessThanOriginalPrice() {
        return jpaRepository.findByVariantsPriceLessThanBasePrice();
    }

    @Override
    public List<Product> findByPriceRange(double minPrice, double maxPrice) {
        return jpaRepository.findByPriceAmountBetween(
                BigDecimal.valueOf(minPrice),
                BigDecimal.valueOf(maxPrice)
        );
    }

    @Override
    public List<Product> findByNameContaining(String name) {
        return jpaRepository.findByNameValueContainingIgnoreCase(name);
    }

    @Override
    public List<Product> findByValidityPeriodContaining(LocalDateTime date) {
        return jpaRepository.findByCreationDateLessThanEqualAndModificationDateGreaterThanEqual(date, date);
    }

    @Override
    public List<Product> findByCreatedAtAfter(LocalDateTime since) {
        return jpaRepository.findByCreationDateAfter(since);
    }

    @Override
    public List<Product> findByOrderByViewCountDesc() {
        return jpaRepository.findAll(Sort.by(Sort.Direction.DESC, "modificationDate"));
    }

    @Override
    public List<Product> findActiveProducts() {
        return jpaRepository.findByActiveTrue();
    }

    @Override
    public List<Product> findRelatedProducts(UUID categoryId, Set<ProductAttribute> attributes, UUID excludeProductId, int limit) {
        return jpaRepository.findRelatedProducts(categoryId, attributes, excludeProductId, limit);
    }

    // Méthodes de recherche Elasticsearch optimisées
    @Override
    public List<Product> searchProductsWithFilters(String query, Map<String, Object> filters,
                                                   String sortField, SortOrder sortOrder,
                                                   int page, int size) {
        try {
            if (page * size > MAX_FROM_SIZE) {
                log.warn("Requested page/size exceeds maximum allowed depth");
                return Collections.emptyList();
            }

            BoolQuery.Builder boolQuery = new BoolQuery.Builder();

            if (query != null && !query.trim().isEmpty()) {
                boolQuery.must(Query.of(q -> q.multiMatch(m -> m
                        .query(query)
                        .fields("name^2", "description", "attributes.value")
                )));
            }

            filters.forEach((key, value) -> {
                if (value != null) {
                    boolQuery.filter(Query.of(q -> q.term(t -> t
                            .field(key)
                            .value(String.valueOf(value))
                    )));
                }
            });

            SearchRequest request = SearchRequest.of(s -> s
                    .index(PRODUCTS_INDEX)
                    .query(Query.of(q -> q.bool(boolQuery.build())))
                    .from(page * size)
                    .size(size)
                    .sort(sort -> sort.field(f -> f
                            .field(sortField != null ? sortField : "_score")
                            .order(sortOrder != null ? sortOrder : SortOrder.Desc)
                    ))
            );

            SearchResponse<Product> response = elasticsearchClient.search(request, Product.class);
            return response.hits().hits().stream()
                    .map(Hit::source)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error searching products with filters", e);
            return Collections.emptyList();
        }
    }

    @Override
    public Page<Product> searchProducts(String query, UUID categoryId, UUID collectionId,
                                        Price minPrice, Price maxPrice, Set<ProductAttribute> attributes,
                                        boolean inStock, LocalDateTime date, Pageable pageable) {
        try {
            if (pageable.getOffset() > MAX_FROM_SIZE) {
                log.warn("Requested page/size exceeds maximum allowed depth");
                return Page.empty();
            }

            BoolQuery.Builder boolQuery = buildSearchQuery(query, categoryId, collectionId,
                    minPrice, maxPrice, attributes, inStock, date);

            SearchRequest request = SearchRequest.of(s -> s
                    .index(PRODUCTS_INDEX)
                    .query(Query.of(q -> q.bool(boolQuery.build())))
                    .from((int) pageable.getOffset())
                    .size(pageable.getPageSize())
                    .sort(sort -> sort.field(f -> f
                            .field(pageable.getSort().stream()
                                    .findFirst()
                                    .map(order -> order.getProperty())
                                    .orElse("_score"))
                            .order(pageable.getSort().stream()
                                    .findFirst()
                                    .map(order -> order.isAscending() ? SortOrder.Asc : SortOrder.Desc)
                                    .orElse(SortOrder.Desc))
                    ))
            );

            SearchResponse<Product> response = elasticsearchClient.search(request, Product.class);
            List<Product> products = response.hits().hits().stream()
                    .map(Hit::source)
                    .collect(Collectors.toList());

            long total = response.hits().total() != null ? response.hits().total().value() : 0;
            return new PageImpl<>(products, pageable, total);
        } catch (Exception e) {
            log.error("Error in advanced search", e);
            return Page.empty();
        }
    }

    private BoolQuery.Builder buildSearchQuery(String query, UUID categoryId, UUID collectionId,
                                               Price minPrice, Price maxPrice, Set<ProductAttribute> attributes,
                                               boolean inStock, LocalDateTime date) {
        BoolQuery.Builder boolQuery = new BoolQuery.Builder();

        if (query != null && !query.trim().isEmpty()) {
            boolQuery.must(Query.of(q -> q.multiMatch(m -> m
                    .query(query)
                    .fields("name^2", "description", "attributes.value")
            )));
        }

        if (categoryId != null) {
            boolQuery.filter(Query.of(q -> q.term(t -> t
                    .field("categoryId")
                    .value(categoryId.toString())
            )));
        }

        if (collectionId != null) {
            boolQuery.filter(Query.of(q -> q.term(t -> t
                    .field("collectionIds")
                    .value(collectionId.toString())
            )));
        }

        if (minPrice != null && maxPrice != null) {
            boolQuery.filter(Query.of(q -> q
                    .range(r -> r
                            .number(n -> n
                                .field("price.amount")
                                .gte(minPrice.getAmount().doubleValue())
                                .lte(maxPrice.getAmount().doubleValue())
                            )
                    )
            ));
        }

        if (inStock) {
            boolQuery.filter(Query.of(q -> q.nested(n -> n
                    .path("variants")
                    .query(Query.of(q2 -> q2.term(t -> t
                            .field("variants.stockStatus")
                            .value("IN_STOCK")
                    )))
            )));
        }

        if (attributes != null && !attributes.isEmpty()) {
            attributes.forEach(attr -> {
                boolQuery.filter(Query.of(q -> q.nested(n -> n
                        .path("attributes")
                        .query(Query.of(q2 -> q2.bool(b -> b
                                .must(Query.of(m -> m.term(t -> t
                                        .field("attributes.name")
                                        .value(attr.getName())
                                )))
                                .must(Query.of(m -> m.term(t -> t
                                        .field("attributes.value")
                                        .value(attr.getValue())
                                )))
                        )))
                )));
            });
        }

        if (date != null) {
            boolQuery.filter(Query.of(q -> q.range(r -> r
                    .date(d -> d
                        .field("creationDate")
                        .gte(date.toString())
                        .lte(date.toString())
                    )
            )));
        }

        return boolQuery;
    }

    @Override
    public List<Product> findSimilarProducts(UUID productId, int size) {
        try {
            SearchRequest request = SearchRequest.of(s -> s
                    .index(PRODUCTS_INDEX)
                    .size(size)
                    .query(Query.of(q -> q.moreLikeThis(mlt -> mlt
                            .like(like -> like.document(doc -> doc
                                    .index(PRODUCTS_INDEX)
                                    .id(productId.toString())
                            ))
                            .fields("name", "description", "attributes.value")
                            .minTermFreq(1)
                            .maxQueryTerms(12)
                    )))
            );

            SearchResponse<Product> response = elasticsearchClient.search(request, Product.class);
            return response.hits().hits().stream()
                    .map(Hit::source)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error finding similar products", e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<Product> findByScoreGreaterThan(String query, float minScore) {
        try {
            SearchRequest request = SearchRequest.of(s -> s
                    .index(PRODUCTS_INDEX)
                    .query(Query.of(q -> q.multiMatch(m -> m
                            .query(query)
                            .fields("name^2", "description", "attributes.value")
                    )))
                    .minScore((double) minScore)
            );

            SearchResponse<Product> response = elasticsearchClient.search(request, Product.class);
            return response.hits().hits().stream()
                    .map(Hit::source)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error searching products by score", e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<Product> findBySuggestions(String query) {
        try {
            SearchRequest request = SearchRequest.of(s -> s
                    .index(PRODUCTS_INDEX)
                    .suggest(suggest -> suggest
                            .text(query)
                            .suggesters("product_suggest", suggester -> suggester
                                    .completion(completion -> completion
                                            .field("suggest")
                                            .size(10)
                                    )
                            )
                    )
            );

            SearchResponse<Product> response = elasticsearchClient.search(request, Product.class);
            return response.hits().hits().stream()
                    .map(Hit::source)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error getting suggestions", e);
            return Collections.emptyList();
        }
    }

    // Méthodes d'agrégation (implémentations simplifiées)
    @Override
    public List<Product> findByWithCategoryAggregation(String query) {
        try {
            SearchRequest request = SearchRequest.of(s -> s
                    .index(PRODUCTS_INDEX)
                    .size(0)
                    .aggregations("categories", a -> a
                            .terms(t -> t
                                    .field("categoryId")
                                    .size(10)
                            )
                    )
            );

            SearchResponse<Product> response = elasticsearchClient.search(request, Product.class);
            return searchProductsWithFilters(query, Collections.emptyMap(), null, null, 0, 50);
        } catch (Exception e) {
            log.error("Error getting category aggregation", e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<Product> findByWithPriceAggregation(String query) {
        try {
            SearchRequest request = SearchRequest.of(s -> s
                    .index(PRODUCTS_INDEX)
                    .size(0)
                    .aggregations("price_ranges", a -> a
                            .histogram(h -> h
                                    .field("variants.price.amount")
                                    .interval(100.0)
                            )
                    )
            );

            SearchResponse<Product> response = elasticsearchClient.search(request, Product.class);
            return searchProductsWithFilters(query, Collections.emptyMap(), "variants.price.amount", SortOrder.Asc, 0, 50);
        } catch (Exception e) {
            log.error("Error getting price aggregation", e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<Product> findByWithAttributesAggregation(String query) {
        try {
            SearchRequest request = SearchRequest.of(s -> s
                    .index(PRODUCTS_INDEX)
                    .size(0)
                    .aggregations("attributes", a -> a
                            .nested(n -> n
                                    .path("attributes")
                            )
                            .aggregations("names", na -> na
                                    .terms(t -> t
                                            .field("attributes.name")
                                            .size(10)
                                    )
                            )
                    )
            );

            SearchResponse<Product> response = elasticsearchClient.search(request, Product.class);
            return searchProductsWithFilters(query, Collections.emptyMap(), null, null, 0, 50);
        } catch (Exception e) {
            log.error("Error getting attributes aggregation", e);
            return Collections.emptyList();
        }
    }

    // Méthodes paginées
    @Override
    public Page<Product> findByCategoryId(UUID categoryId, Pageable pageable) {
        return jpaRepository.findByCategoryId(categoryId, pageable);
    }

    @Override
    public Page<Product> findByCollectionId(UUID collectionId, Pageable pageable) {
        return jpaRepository.findByCollectionIdsContaining(collectionId, pageable);
    }

    @Override
    public Page<Product> findActiveProducts(Pageable pageable) {
        return jpaRepository.findByActiveTrue(pageable);
    }

    @Override
    public Page<Product> findByAttributes(List<String> attributeNames, List<String> attributeValues, Pageable pageable) {
        return jpaRepository.findByAttributesNameInAndAttributesValueIn(attributeNames, attributeValues, pageable);
    }

    @Override
    public Page<Product> findByAttributes(Map<String, String> attributes, Pageable pageable) {
        return jpaRepository.findByAttributesMap(attributes, pageable);
    }

    @Override
    public Page<Product> findByAttributesValue(String value, Pageable pageable) {
        return jpaRepository.findByAttributesValueContaining(value, pageable);
    }

    @Override
    public Page<Product> findByVariantsSku(String sku, Pageable pageable) {
        return jpaRepository.findByVariantsSkuValue(sku, pageable);
    }

    @Override
    public Page<Product> findByVariantsDimensions(JewelryDimensions dimensions, Pageable pageable) {
        return jpaRepository.findByVariantsDimensions(dimensions, pageable);
    }

    @Override
    public Page<Product> findByVariantsPriceBetween(Price minPrice, Price maxPrice, Pageable pageable) {
        return jpaRepository.findByVariantsPriceAmountBetween(
                minPrice.getAmount(), maxPrice.getAmount(), pageable);
    }

    @Override
    public Page<Product> findByVariantsStockStatus(ProductVariant.StockStatus status, Pageable pageable) {
        return jpaRepository.findByVariantsStockStatus(status, pageable);
    }

    @Override
    public Page<Product> findByVariantsPriceLessThanOriginalPrice(Pageable pageable) {
        return jpaRepository.findByVariantsPriceLessThanBasePrice(pageable);
    }

    @Override
    public Page<Product> findByPriceRange(double minPrice, double maxPrice, Pageable pageable) {
        return jpaRepository.findByPriceAmountBetween(
                BigDecimal.valueOf(minPrice),
                BigDecimal.valueOf(maxPrice),
                pageable
        );
    }

    @Override
    public Page<Product> findByNameContaining(String name, Pageable pageable) {
        return jpaRepository.findByNameValueContainingIgnoreCase(name, pageable);
    }

    @Override
    public Page<Product> findByValidityPeriodContaining(LocalDateTime date, Pageable pageable) {
        return jpaRepository.findByCreationDateLessThanEqualAndModificationDateGreaterThanEqual(date, date, pageable);
    }

    @Override
    public Page<Product> findByCreatedAtAfter(LocalDateTime since, Pageable pageable) {
        return jpaRepository.findByCreationDateAfter(since, pageable);
    }

    @Override
    public Page<Product> findByOrderByViewCountDesc(Pageable pageable) {
        return jpaRepository.findAllByOrderByModificationDateDesc(pageable);
    }

    // Vérifications
    @Override
    public boolean existsBySku(String sku) {
        return jpaRepository.existsBySku(sku);
    }

    @Override
    public Page<Product> findByActiveTrue(Pageable pageable) {
        return jpaRepository.findByActiveTrue(pageable);
    }

    @Override
    public Page<Product> searchSimilar(Product product, String[] fields, Pageable pageable) {
        List<Product> similar = findSimilarProducts(product.getId(), pageable.getPageSize());
        return new PageImpl<>(similar, pageable, similar.size());
    }


}