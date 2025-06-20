package com.enaya.product_service.application.service;

import com.enaya.product_service.application.dto.request.CreateCategoryRequest;
import com.enaya.product_service.application.dto.request.UpdateCategoryRequest;
import com.enaya.product_service.application.dto.response.CategoryResponse;
import com.enaya.product_service.application.dto.response.CategoryTreeResponse;
import com.enaya.product_service.application.mapper.CategoryMapper;
import com.enaya.product_service.domain.event.category.CategoryCreated;
import com.enaya.product_service.domain.event.category.CategoryUpdated;
import com.enaya.product_service.domain.model.category.Category;
import com.enaya.product_service.domain.model.product.Product;
import com.enaya.product_service.domain.repository.CategoryRepository;
import com.enaya.product_service.domain.service.CategoryDomainService;
import com.enaya.product_service.domain.service.ProductSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryApplicationService {

    private final CategoryRepository categoryRepository;
    private final CategoryDomainService categoryDomainService;
    private final CategoryMapper categoryMapper;
    private final ProductSearchService productSearchService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        // Check if category with same name exists
        if (categoryRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("A category with this name already exists");
        }

        Category category;
        if (request.getParentId() != null) {
            // Create child category
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException("Parent category not found"));
            
            category = categoryDomainService.createChildCategory(
                    request.getName(),
                    request.getDescription(),
                    parent.getId(),
                    parent.getFullPath(),
                    parent.getLevel()
            );
            
            // Update parent's child list
            categoryDomainService.addChildCategory(parent, category.getId());
            categoryRepository.save(parent);
        } else {
            // Create root category
            category = categoryDomainService.createRootCategory(
                    request.getName(),
                    request.getDescription()
            );
        }

        // Set additional properties
        if (request.getImageUrl() != null) {
            categoryDomainService.setCategoryImage(category, request.getImageUrl());
        }
        if (request.getDisplayOrder() != null) {
            categoryDomainService.updateCategoryDisplayOrder(category, request.getDisplayOrder());
        }
        if (request.getVisibleInMenu() != null && !request.getVisibleInMenu()) {
            categoryDomainService.hideCategoryFromMenu(category);
        }

        Category savedCategory = categoryRepository.save(category);
        
        // Publish category created event
        eventPublisher.publishEvent(CategoryCreated.from(savedCategory));
        
        return categoryMapper.toResponse(savedCategory);
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategory(UUID id) {
        return categoryRepository.findById(id)
                .map(categoryMapper::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getActiveCategories() {
        return categoryRepository.findActiveCategories().stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getRootCategories() {
        return categoryRepository.findRootCategories().stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getChildCategories(UUID parentId) {
        return categoryRepository.findByParentId(parentId).stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getVisibleInMenuCategories() {
        return categoryRepository.findVisibleInMenu().stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> searchCategories(String name) {
        return categoryRepository.findByNameContaining(name).stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    @Transactional
    public CategoryResponse updateCategory(UUID id, UpdateCategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        // Update basic info
        categoryDomainService.updateCategoryBasicInfo(category, request.getName(), request.getDescription());

        // Update metadata if provided


        // Update image if provided
        if (request.getImageUrl() != null) {
            categoryDomainService.setCategoryImage(category, request.getImageUrl());
        }

        // Update display order if provided
        if (request.getDisplayOrder() != null) {
            categoryDomainService.updateCategoryDisplayOrder(category, request.getDisplayOrder());
        }

        // Update active status if provided
        if (request.getActive() != null) {
            if (request.getActive()) {
                categoryDomainService.activateCategory(category);
            } else {
                categoryDomainService.deactivateCategory(category);
            }
        }

        // Update menu visibility if provided
        if (request.getVisibleInMenu() != null) {
            if (request.getVisibleInMenu()) {
                categoryDomainService.showCategoryInMenu(category);
            } else {
                categoryDomainService.hideCategoryFromMenu(category);
            }
        }

        Category updatedCategory = categoryRepository.save(category);
        
        // Publish category updated event
        eventPublisher.publishEvent(CategoryUpdated.from(updatedCategory));

        return categoryMapper.toResponse(updatedCategory);
    }

    @Transactional
    public void deleteCategory(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        if (!categoryDomainService.canDeleteCategory(category)) {
            throw new IllegalStateException("Cannot delete category: it has child categories or is referenced by products");
        }

        // If category has a parent, remove it from parent's child list
        if (category.getParentId() != null) {
            Category parent = categoryRepository.findById(category.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException("Parent category not found"));
            categoryDomainService.removeChildCategory(parent, category.getId());
            categoryRepository.save(parent);
        }

        categoryRepository.delete(id);
    }

    @Transactional(readOnly = true)
    public CategoryTreeResponse getCategoryTree(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        List<CategoryTreeResponse> children = new ArrayList<>();
        int totalProducts = 0;

        // Get direct products in this category with pagination
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE); // Get all products
        Page<Product> categoryProducts = productSearchService.findProductsByCategory(category.getId(), pageable);
        totalProducts += categoryProducts.getTotalElements();

        // Process child categories
        for (UUID childId : category.getChildCategoryIds()) {
            Category child = categoryRepository.findById(childId)
                    .orElseThrow(() -> new IllegalArgumentException("Child category not found"));
            CategoryTreeResponse childTree = getCategoryTree(child.getId());
            children.add(childTree);
            totalProducts += childTree.getTotalProducts(); // Add products from child categories
        }

        return categoryMapper.toTreeResponse(category, children, totalProducts);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategoryAncestors(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        return categoryRepository.findAncestors(category.getFullPath()).stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategoryDescendants(UUID id) {
        return categoryRepository.findDescendants(id).stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategoriesByLevel(int level) {
        return categoryRepository.findByLevel(level).stream()
                .map(categoryMapper::toResponse)
                .toList();
    }
}
