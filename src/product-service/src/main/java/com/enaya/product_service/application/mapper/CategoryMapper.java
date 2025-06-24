package com.enaya.product_service.application.mapper;

import com.enaya.product_service.application.dto.request.CreateCategoryRequest;
import com.enaya.product_service.application.dto.request.UpdateCategoryRequest;
import com.enaya.product_service.application.dto.response.CategoryResponse;
import com.enaya.product_service.application.dto.response.CategoryTreeResponse;
import com.enaya.product_service.domain.model.category.Category;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CategoryMapper {

    public CategoryResponse toResponse(Category category) {
        if (category == null) {
            return null;
        }

        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .slug(category.getSlug())
                .parentId(category.getParentId())
                .fullPath(category.getFullPath())
                .level(category.getLevel())
                .childCategoryIds(category.getChildCategoryIds())
                // .metadata(category.getMetadata()) // This field was causing issues
                .imageUrl(category.getImageUrl())
                .displayOrder(category.getDisplayOrder())
                .active(category.isActive())
                .visibleInMenu(category.isVisibleInMenu())
                .creationDate(category.getCreationDate())
                .modificationDate(category.getModificationDate())
                .version(category.getVersion())
                .build();
    }

    public CategoryTreeResponse toTreeResponse(Category category, List<CategoryTreeResponse> children, int totalProducts) {
        if (category == null) {
            return null;
        }

        return CategoryTreeResponse.builder()
                .category(toResponse(category))
                .children(children)
                .totalProducts(totalProducts)
                .hasChildren(!children.isEmpty())
                .build();
    }

    public Category toEntity(CreateCategoryRequest request) {
        if (request == null) {
            return null;
        }

        return Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .parentId(request.getParentId())
                .imageUrl(request.getImageUrl())
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .visibleInMenu(request.getVisibleInMenu() != null ? request.getVisibleInMenu() : true)
                .build();
    }

    public void updateEntity(Category category, UpdateCategoryRequest request) {
        if (category == null || request == null) {
            return;
        }

        // Mise à jour des informations de base
        category.updateBasicInfo(request.getName(), request.getDescription());



        // Mise à jour de l'ordre d'affichage si présent
        if (request.getDisplayOrder() != null) {
            category.updateDisplayOrder(request.getDisplayOrder());
        }

        // Mise à jour de l'image si présente
        if (request.getImageUrl() != null) {
            category.setImage(request.getImageUrl());
        }

        // Mise à jour du statut actif si présent
        if (request.getActive() != null) {
            if (request.getActive()) {
                category.activate();
            } else {
                category.deactivate();
            }
        }

        // Mise à jour de la visibilité dans le menu si présente
        if (request.getVisibleInMenu() != null) {
            if (request.getVisibleInMenu()) {
                category.showInMenu();
            } else {
                category.hideFromMenu();
            }
        }
    }
} 