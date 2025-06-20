package com.enaya.product_service.presentation.controller;

import com.enaya.product_service.application.dto.request.CreateCategoryRequest;
import com.enaya.product_service.application.dto.request.UpdateCategoryRequest;
import com.enaya.product_service.application.dto.response.CategoryResponse;
import com.enaya.product_service.application.dto.response.CategoryTreeResponse;
import com.enaya.product_service.application.service.CategoryApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryApplicationService categoryApplicationService;

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        return new ResponseEntity<>(categoryApplicationService.createCategory(request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategory(@PathVariable UUID id) {
        return ResponseEntity.ok(categoryApplicationService.getCategory(id));
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        return ResponseEntity.ok(categoryApplicationService.getAllCategories());
    }

    @GetMapping("/active")
    public ResponseEntity<List<CategoryResponse>> getActiveCategories() {
        return ResponseEntity.ok(categoryApplicationService.getActiveCategories());
    }

    @GetMapping("/root")
    public ResponseEntity<List<CategoryResponse>> getRootCategories() {
        return ResponseEntity.ok(categoryApplicationService.getRootCategories());
    }

    @GetMapping("/{parentId}/children")
    public ResponseEntity<List<CategoryResponse>> getChildCategories(@PathVariable UUID parentId) {
        return ResponseEntity.ok(categoryApplicationService.getChildCategories(parentId));
    }

    @GetMapping("/menu")
    public ResponseEntity<List<CategoryResponse>> getVisibleInMenuCategories() {
        return ResponseEntity.ok(categoryApplicationService.getVisibleInMenuCategories());
    }

    @GetMapping("/search")
    public ResponseEntity<List<CategoryResponse>> searchCategories(@RequestParam String name) {
        return ResponseEntity.ok(categoryApplicationService.searchCategories(name));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCategoryRequest request) {
        return ResponseEntity.ok(categoryApplicationService.updateCategory(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable UUID id) {
        categoryApplicationService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/tree")
    public ResponseEntity<CategoryTreeResponse> getCategoryTree(@PathVariable UUID id) {
        return ResponseEntity.ok(categoryApplicationService.getCategoryTree(id));
    }

    @GetMapping("/{id}/ancestors")
    public ResponseEntity<List<CategoryResponse>> getCategoryAncestors(@PathVariable UUID id) {
        return ResponseEntity.ok(categoryApplicationService.getCategoryAncestors(id));
    }

    @GetMapping("/{id}/descendants")
    public ResponseEntity<List<CategoryResponse>> getCategoryDescendants(@PathVariable UUID id) {
        return ResponseEntity.ok(categoryApplicationService.getCategoryDescendants(id));
    }

    @GetMapping("/level/{level}")
    public ResponseEntity<List<CategoryResponse>> getCategoriesByLevel(@PathVariable int level) {
        return ResponseEntity.ok(categoryApplicationService.getCategoriesByLevel(level));
    }
}
