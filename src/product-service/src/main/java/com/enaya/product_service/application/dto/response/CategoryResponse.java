package com.enaya.product_service.application.dto.response;

import com.enaya.product_service.domain.model.category.valueobjects.CategoryMetadata;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class CategoryResponse {
    private UUID id;
    private String name;
    private String description;
    private String slug;
    private UUID parentId;
    private String fullPath;
    private int level;
    private List<UUID> childCategoryIds;
    // private CategoryMetadata metadata;
    private String imageUrl;
    private int displayOrder;
    private boolean active;
    private boolean visibleInMenu;
    private LocalDateTime creationDate;
    private LocalDateTime modificationDate;
    private long version;
} 