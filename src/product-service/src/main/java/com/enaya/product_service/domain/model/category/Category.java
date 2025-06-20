package com.enaya.product_service.domain.model.category;

import com.enaya.product_service.domain.model.category.valueobjects.CategoryMetadata;
import com.enaya.product_service.infrastructure.persistence.converter.UuidListConverter;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(name = "parent_id")
    private UUID parentId;

    @Column(nullable = false)
    private int level;

    @Column(name = "path", nullable = false)
    private String path;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "display_order")
    private int displayOrder;

    @Column(name = "image_url")
    private String imageUrl;

    @CreationTimestamp
    @Column(name = "creation_date", nullable = false, updatable = false)
    private LocalDateTime creationDate;

    @UpdateTimestamp
    @Column(name = "modification_date", nullable = false)
    private LocalDateTime modificationDate;

    @Version
    private Long version;

    @Column(name = "slug")
    private String slug;

    @Column(name = "child_category_ids")
    @Convert(converter = UuidListConverter.class)
    private List<UUID> childCategoryIds = new ArrayList<>();

    @Column(name = "full_path")
    private String fullPath;

    @Column(name = "visible_in_menu")
    private boolean visibleInMenu = true;

    @Column(name = "metadata")
    private CategoryMetadata metadata;

    @Builder
    private Category(String name, String description, String slug,
                     UUID parentId, List<UUID> childCategoryIds,
                     int displayOrder, String fullPath, int level,
                     CategoryMetadata metadata, boolean active,
                     boolean visibleInMenu, String imageUrl) {
        this.name = validateName(name);
        this.description = description;
        this.slug = generateSlugIfNeeded(slug, name);
        this.path = this.slug;
        log.debug("Category constructor: Generated slug: '{}', Assigned path: '{}'", this.slug, this.path);
        this.parentId = parentId;
        this.childCategoryIds = childCategoryIds != null ?
                new ArrayList<>(childCategoryIds) : new ArrayList<>();
        this.displayOrder = Math.max(0, displayOrder);
        this.fullPath = fullPath;
        this.level = Math.max(0, level);
        this.metadata = metadata;
        this.active = active;
        this.visibleInMenu = visibleInMenu;
        this.imageUrl = imageUrl;
        this.creationDate = LocalDateTime.now();
        this.modificationDate = LocalDateTime.now();
        this.version = 0L;
    }

    public static Category createRoot(String name, String description) {
        return Category.builder()
                .name(name)
                .description(description)
                .level(0)
                .fullPath(name)
                .active(true)
                .visibleInMenu(true)
                .displayOrder(0)
                .build();
    }

    public static Category createChild(String name, String description, UUID parentId,
                                       String parentPath, int parentLevel) {
        return Category.builder()
                .name(name)
                .description(description)
                .parentId(parentId)
                .level(parentLevel + 1)
                .fullPath(parentPath + " > " + name)
                .active(true)
                .visibleInMenu(true)
                .displayOrder(0)
                .build();
    }

    public void updateBasicInfo(String name, String description) {
        this.name = validateName(name);
        this.description = description;
        this.slug = generateSlugIfNeeded(null, name);
        updateModificationDate();
    }

    public void updateMetadata(CategoryMetadata metadata) {
        this.metadata = metadata;
        updateModificationDate();
    }

    public void updateDisplayOrder(int newOrder) {
        this.displayOrder = Math.max(0, newOrder);
        updateModificationDate();
    }

    public void setImage(String imageUrl) {
        this.imageUrl = imageUrl;
        updateModificationDate();
    }

    public void removeImage() {
        this.imageUrl = null;
        updateModificationDate();
    }

    public void activate() {
        this.active = true;
        updateModificationDate();
    }

    public void deactivate() {
        this.active = false;
        updateModificationDate();
    }

    public void showInMenu() {
        this.visibleInMenu = true;
        updateModificationDate();
    }

    public void hideFromMenu() {
        this.visibleInMenu = false;
        updateModificationDate();
    }

    public void addChildCategory(UUID childId) {
        if (childId != null && !this.childCategoryIds.contains(childId)) {
            this.childCategoryIds.add(childId);
            updateModificationDate();
        }
    }

    public void removeChildCategory(UUID childId) {
        if (this.childCategoryIds.remove(childId)) {
            updateModificationDate();
        }
    }

    public void updateHierarchyInfo(String newFullPath, int newLevel) {
        this.fullPath = newFullPath;
        this.level = Math.max(0, newLevel);
        updateModificationDate();
    }

    public void moveToParent(UUID newParentId, String newFullPath, int newLevel) {
        this.parentId = newParentId;
        this.fullPath = newFullPath;
        this.level = Math.max(0, newLevel);
        updateModificationDate();
    }

    public boolean isRootCategory() {
        return this.parentId == null;
    }

    public boolean hasChildren() {
        return !this.childCategoryIds.isEmpty();
    }

    public boolean hasParent() {
        return this.parentId != null;
    }

    public boolean isLeafCategory() {
        return this.childCategoryIds.isEmpty();
    }

    public boolean canBeDeleted() {
        return this.childCategoryIds.isEmpty();
    }

    public boolean isDescendantOf(UUID ancestorId) {
        if (ancestorId == null || this.fullPath == null) {
            return false;
        }
        // Simplified check — in real usage you'd traverse the hierarchy
        return this.parentId != null && this.parentId.equals(ancestorId);
    }

    public int getMaxDepthFromHere() {
        // Typically this would be calculated by traversing child categories
        // For now, we just return a simple approximation
        return Math.max(0, 10 - this.level); // Assuming max depth is 10
    }

    public List<String> getPathSegments() {
        if (this.fullPath == null) {
            return new ArrayList<>();
        }
        return List.of(this.fullPath.split(" > "));
    }

    public boolean hasImage() {
        return this.imageUrl != null && !this.imageUrl.trim().isEmpty();
    }

    public boolean hasMetadata() {
        return this.metadata != null;
    }

    private void updateModificationDate() {
        this.modificationDate = LocalDateTime.now();
        this.version++;
    }

    private String validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be null or empty");
        }
        if (name.length() > 255) {
            throw new IllegalArgumentException("Category name cannot exceed 255 characters");
        }
        return name.trim();
    }

    private String generateSlugIfNeeded(String providedSlug, String name) {
        if (providedSlug != null && !providedSlug.trim().isEmpty()) {
            return validateAndNormalizeSlug(providedSlug);
        }

        // Generate slug from name
        return name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "") // Remove special characters
                .replaceAll("\\s+", "-") // Replace spaces with hyphens
                .replaceAll("-+", "-") // Replace multiple hyphens with single
                .replaceAll("^-|-$", ""); // Remove leading/trailing hyphens
    }

    private String validateAndNormalizeSlug(String slug) {
        if (slug == null || slug.trim().isEmpty()) {
            throw new IllegalArgumentException("Slug cannot be null or empty");
        }

        String normalizedSlug = slug.toLowerCase().trim();
        if (!normalizedSlug.matches("^[a-z0-9-]+$")) {
            throw new IllegalArgumentException("Slug must contain only lowercase letters, numbers, and hyphens");
        }

        if (normalizedSlug.startsWith("-") || normalizedSlug.endsWith("-")) {
            throw new IllegalArgumentException("Slug cannot start or end with a hyphen");
        }

        if (normalizedSlug.contains("--")) {
            throw new IllegalArgumentException("Slug cannot contain consecutive hyphens");
        }

        if (normalizedSlug.length() > 100) {
            throw new IllegalArgumentException("Slug cannot exceed 100 characters");
        }

        return normalizedSlug;
    }
}