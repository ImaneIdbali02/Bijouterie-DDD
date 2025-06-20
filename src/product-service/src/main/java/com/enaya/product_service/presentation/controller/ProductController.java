package com.enaya.product_service.presentation.controller;

import com.enaya.product_service.application.dto.request.CreateProductRequest;
import com.enaya.product_service.application.dto.request.UpdateProductRequest;
import com.enaya.product_service.application.dto.response.ProductResponse;
import com.enaya.product_service.application.service.ProductApplicationService;
import com.enaya.product_service.domain.model.product.ProductVariant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductApplicationService productApplicationService;

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@RequestBody CreateProductRequest request) {
        log.info("Creating new product: {}", request);
        ProductResponse response = productApplicationService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{productId}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable UUID productId,
            @RequestBody UpdateProductRequest request) {
        log.info("Updating product: {} with data: {}", productId, request);
        ProductResponse response = productApplicationService.updateProduct(productId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable UUID productId) {
        log.info("Deleting product: {}", productId);
        productApplicationService.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable UUID productId) {
        log.info("Getting product: {}", productId);
        ProductResponse response = productApplicationService.getProduct(productId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Getting all products - page: {}, size: {}", page, size);
        Page<ProductResponse> response = productApplicationService.getAllProducts(page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/active")
    public ResponseEntity<Page<ProductResponse>> getActiveProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Getting active products - page: {}, size: {}", page, size);
        Page<ProductResponse> response = productApplicationService.getActiveProducts(page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Page<ProductResponse>> getProductsByCategory(
            @PathVariable UUID categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Getting products by category: {} - page: {}, size: {}", categoryId, page, size);
        Page<ProductResponse> response = productApplicationService.getProductsByCategory(categoryId, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/collection/{collectionId}")
    public ResponseEntity<Page<ProductResponse>> getProductsByCollection(
            @PathVariable UUID collectionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Getting products by collection: {} - page: {}, size: {}", collectionId, page, size);
        Page<ProductResponse> response = productApplicationService.getProductsByCollection(collectionId, page, size);
        return ResponseEntity.ok(response);
    }

    }