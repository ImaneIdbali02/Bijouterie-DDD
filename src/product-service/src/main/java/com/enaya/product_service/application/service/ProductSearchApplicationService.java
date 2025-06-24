package com.enaya.product_service.application.service;

import com.enaya.product_service.application.dto.response.ProductResponse;
import com.enaya.product_service.application.mapper.ProductMapper;
import com.enaya.product_service.domain.model.product.Product;
import com.enaya.product_service.domain.model.product.valueobjects.Price;
import com.enaya.product_service.domain.model.product.valueobjects.ProductAttribute;
import com.enaya.product_service.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductSearchApplicationService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Transactional(readOnly = true)
    public Page<ProductResponse> searchProducts(
            String query,
            UUID categoryId,
            UUID collectionId,
            Double minPrice,
            Double maxPrice,
            Map<String, String> attributes,
            Boolean inStock,
            String sortField,
            String sortDirection,
            Pageable pageable) {
        
        log.info("Searching products with query: {}, category: {}, collection: {}", 
                query, categoryId, collectionId);

        // Conversion des paramètres en types de domaine
        Price minPriceObj = minPrice != null ? Price.of(BigDecimal.valueOf(minPrice), Currency.getInstance("MAD")) : null;
        Price maxPriceObj = maxPrice != null ? Price.of(BigDecimal.valueOf(maxPrice), Currency.getInstance("MAD")) : null;

        // Conversion des attributs
        Set<ProductAttribute> productAttributes = attributes != null ? 
            attributes.entrySet().stream()
                .map(e -> ProductAttribute.of(e.getKey(), e.getValue()))
                .collect(Collectors.toSet()) : null;

        // Application du tri si spécifié
        if (sortField != null && sortDirection != null) {
            Direction direction = Direction.fromString(sortDirection);
            pageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(direction, sortField)
            );
        }

        // Recherche des produits
        Page<Product> products = productRepository.searchProducts(
                query,
                categoryId,
                collectionId,
                minPriceObj,
                maxPriceObj,
                productAttributes,
                inStock != null ? inStock : false,  // Valeur par défaut si null
                LocalDateTime.now(),
                pageable
        );

        return products.map(productMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> findSimilarProducts(UUID productId, int size) {
        log.info("Finding similar products for product: {}", productId);
        
        // Recherche des produits par catégorie et attributs similaires
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        List<Product> similarProducts = productRepository.findRelatedProducts(
                product.getCategoryId(),
                new HashSet<>(product.getAttributes()),
                productId,
                size
        );

        return similarProducts.stream()
                .map(productMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<String> getSuggestions(String query) {
        log.info("Getting suggestions for query: {}", query);
        
        // Recherche des produits par nom contenant la requête
        List<Product> suggestions = productRepository.findByNameContaining(query);
        return suggestions.stream()
                .map(Product::getName)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getSearchAggregations(String query) {
        log.info("Getting search aggregations for query: {}", query);
        
        Map<String, Object> aggregations = new HashMap<>();
        
        // Agrégation par catégorie
        List<Product> products = productRepository.findByCategoryId(null);
        aggregations.put("categories", processCategoryAggregation(products));
        
        // Agrégation par prix
        aggregations.put("prices", processPriceAggregation(products));
        
        // Agrégation par attributs
        aggregations.put("attributes", processAttributesAggregation(products));
        
        return aggregations;
    }

    private Map<String, Long> processCategoryAggregation(List<Product> products) {
        return products.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getCategoryId().toString(),
                        Collectors.counting()
                ));
    }

    private Map<String, Long> processPriceAggregation(List<Product> products) {
        return products.stream()
                .collect(Collectors.groupingBy(
                        p -> getPriceRange(p.getPrice().getAmount()),
                        Collectors.counting()
                ));
    }

    private Map<String, Map<String, Long>> processAttributesAggregation(List<Product> products) {
        Map<String, Map<String, Long>> result = new HashMap<>();
        
        products.forEach(product -> {
            product.getAttributes().forEach(attribute -> {
                result.computeIfAbsent(attribute.getName(), k -> new HashMap<>())
                        .merge(attribute.getValue(), 1L, Long::sum);
            });
        });
        
        return result;
    }

    private String getPriceRange(BigDecimal price) {
        if (price.compareTo(BigDecimal.valueOf(100)) <= 0) {
            return "0-100";
        } else if (price.compareTo(BigDecimal.valueOf(500)) <= 0) {
            return "101-500";
        } else if (price.compareTo(BigDecimal.valueOf(1000)) <= 0) {
            return "501-1000";
        } else {
            return "1000+";
        }
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> searchProductsByName(String name, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = productRepository.findByNameContaining(name, pageable);
        return products.map(productMapper::toResponse);
    }
} 