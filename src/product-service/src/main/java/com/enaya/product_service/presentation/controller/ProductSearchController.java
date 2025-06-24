package com.enaya.product_service.presentation.controller;

import com.enaya.product_service.application.dto.response.ProductResponse;
import com.enaya.product_service.application.service.ProductSearchApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/products/search")
@RequiredArgsConstructor
public class ProductSearchController {

    private final ProductSearchApplicationService productSearchApplicationService;

    @GetMapping
    public ResponseEntity<Page<ProductResponse>> searchProducts(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) UUID collectionId,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Map<String, String> attributes,
            @RequestParam(required = false) Boolean inStock,
            @RequestParam(defaultValue = "name") String sortField,
            @RequestParam(defaultValue = "asc") String sortDirection,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        PageRequest pageRequest = PageRequest.of(page, size, 
            Sort.by(Sort.Direction.fromString(sortDirection), sortField));

        Page<ProductResponse> results = productSearchApplicationService.searchProducts(
                query, categoryId, collectionId, minPrice, maxPrice,
                attributes, inStock, sortField, sortDirection, pageRequest);

        return ResponseEntity.ok(results);
    }

    @GetMapping("/similar/{productId}")
    public ResponseEntity<List<ProductResponse>> findSimilarProducts(
            @PathVariable UUID productId,
            @RequestParam(defaultValue = "5") int size) {
        
        List<ProductResponse> similarProducts = productSearchApplicationService.findSimilarProducts(productId, size);
        return ResponseEntity.ok(similarProducts);
    }

    @GetMapping("/suggestions")
    public ResponseEntity<List<String>> getSuggestions(@RequestParam String query) {
        List<String> suggestions = productSearchApplicationService.getSuggestions(query);
        return ResponseEntity.ok(suggestions);
    }

    @GetMapping("/aggregations")
    public ResponseEntity<Map<String, Object>> getSearchAggregations(@RequestParam String query) {
        Map<String, Object> aggregations = productSearchApplicationService.getSearchAggregations(query);
        return ResponseEntity.ok(aggregations);
    }
} 