package com.enaya.product_service.presentation.controller;

import com.enaya.product_service.application.dto.request.CreateCollectionRequest;
import com.enaya.product_service.application.dto.request.UpdateCollectionRequest;
import com.enaya.product_service.application.dto.response.CollectionResponse;
import com.enaya.product_service.application.service.CollectionApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/collections")
@RequiredArgsConstructor
public class CollectionController {

    private final CollectionApplicationService collectionApplicationService;

    @PostMapping
    public ResponseEntity<CollectionResponse> createCollection(@Valid @RequestBody CreateCollectionRequest request) {
        return new ResponseEntity<>(collectionApplicationService.createCollection(request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CollectionResponse> getCollection(@PathVariable UUID id) {
        return ResponseEntity.ok(collectionApplicationService.getCollection(id));
    }

    @GetMapping
    public ResponseEntity<List<CollectionResponse>> getAllCollections() {
        return ResponseEntity.ok(collectionApplicationService.getAllCollections());
    }

    @GetMapping("/active")
    public ResponseEntity<List<CollectionResponse>> getActiveCollections() {
        return ResponseEntity.ok(collectionApplicationService.getActiveCollections());
    }

    @GetMapping("/search")
    public ResponseEntity<List<CollectionResponse>> searchCollections(@RequestParam String name) {
        return ResponseEntity.ok(collectionApplicationService.searchCollections(name));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CollectionResponse> updateCollection(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCollectionRequest request) {
        return ResponseEntity.ok(collectionApplicationService.updateCollection(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCollection(@PathVariable UUID id) {
        collectionApplicationService.deleteCollection(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<CollectionResponse> publishCollection(@PathVariable UUID id) {
        return ResponseEntity.ok(collectionApplicationService.publishCollection(id));
    }

    @PostMapping("/{id}/archive")
    public ResponseEntity<CollectionResponse> archiveCollection(@PathVariable UUID id) {
        return ResponseEntity.ok(collectionApplicationService.archiveCollection(id));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<CollectionResponse>> getCollectionsByProduct(@PathVariable UUID productId) {
        return ResponseEntity.ok(collectionApplicationService.getCollectionsByProduct(productId));
    }

    @PostMapping("/{collectionId}/products/{productId}")
    public ResponseEntity<CollectionResponse> addProductToCollection(
            @PathVariable UUID collectionId,
            @PathVariable UUID productId) {
        return ResponseEntity.ok(collectionApplicationService.addProductToCollection(collectionId, productId));
    }

    @DeleteMapping("/{collectionId}/products/{productId}")
    public ResponseEntity<CollectionResponse> removeProductFromCollection(
            @PathVariable UUID collectionId,
            @PathVariable UUID productId) {
        return ResponseEntity.ok(collectionApplicationService.removeProductFromCollection(collectionId, productId));
    }
}
