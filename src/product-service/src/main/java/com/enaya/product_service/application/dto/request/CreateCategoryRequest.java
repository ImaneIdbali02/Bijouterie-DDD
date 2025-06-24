package com.enaya.product_service.application.dto.request;

import com.enaya.product_service.domain.model.category.valueobjects.CategoryMetadata;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
public class CreateCategoryRequest {
    @NotBlank(message = "Le nom de la catégorie est requis")
    private String name;

    @NotBlank(message = "La description de la catégorie est requise")
    private String description;

    private UUID parentId;
    // private CategoryMetadata metadata;
    private String imageUrl;
    private Integer displayOrder;
    private Boolean visibleInMenu;
} 