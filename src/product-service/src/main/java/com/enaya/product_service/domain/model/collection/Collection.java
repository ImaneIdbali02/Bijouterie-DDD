package com.enaya.product_service.domain.model.collection;

import com.enaya.product_service.domain.model.collection.valueobjects.ImageCollection;
import com.enaya.product_service.domain.model.collection.valueobjects.PeriodCollection;
import com.enaya.product_service.infrastructure.persistence.converter.UuidListConverter;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "collections")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Collection {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(length = 1000)
    private String description;
    
    @Column(name = "slug", unique = true)
    private String slug;
    
    @Embedded
    private PeriodCollection period;
    
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "collection_images", joinColumns = @JoinColumn(name = "collection_id"))
    private List<ImageCollection> images;
    
    @Column(name = "product_ids")
    @Convert(converter = UuidListConverter.class)
    private List<UUID> productIds;
    
    @Column(nullable = false)
    private boolean active = true;
    
    @Column(nullable = false)
    private boolean published = false;
    
    @Column(nullable = false)
    private int priority;
    
    @Column(name = "meta_title", length = 255)
    private String metaTitle;
    
    @Column(name = "meta_description", length = 500)
    private String metaDescription;
    
    @CreationTimestamp
    @Column(name = "creation_date", nullable = false, updatable = false)
    private LocalDateTime creationDate;
    
    @UpdateTimestamp
    @Column(name = "modification_date", nullable = false)
    private LocalDateTime modificationDate;
    
    @Column(name = "publication_date")
    private LocalDateTime publicationDate;
    
    @Version
    private Long version;

    @Builder
    private Collection(UUID id, String name, String description, String slug,
                       PeriodCollection period, List<ImageCollection> images,
                       List<UUID> productIds, boolean active, boolean published,
                       int priority, String metaTitle, String metaDescription) {
        this.id = id != null ? id : UUID.randomUUID();
        this.name = validateName(name);
        this.description = description;
        this.slug = generateSlug(slug, name);
        this.period = period;
        this.images = images != null ? new ArrayList<>(images) : new ArrayList<>();
        this.productIds = productIds != null ? new ArrayList<>(productIds) : new ArrayList<>();
        this.active = active;
        this.published = published;
        this.priority = Math.max(0, priority);
        this.metaTitle = metaTitle;
        this.metaDescription = metaDescription;
        this.creationDate = LocalDateTime.now();
        this.modificationDate = LocalDateTime.now();
        this.version = 0L;
    }

    public static Collection create(String name, String description, PeriodCollection period) {
        return Collection.builder()
                .name(name)
                .description(description)
                .period(period)
                .active(true)
                .published(false)
                .priority(0)
                .build();
    }

    public static Collection createSeasonal(String name, String description,
                                            LocalDateTime startDate, LocalDateTime endDate) {
        PeriodCollection period = PeriodCollection.of(startDate, endDate);
        return create(name, description, period);
    }

    public static Collection createPermanent(String name, String description) {
        return create(name, description, null);
    }

    public void updateBasicInfo(String name, String description) {
        this.name = validateName(name);
        this.description = description;
        this.slug = generateSlug(null, name);
        this.modificationDate = LocalDateTime.now();
        this.version++;
    }

    public void updatePeriod(PeriodCollection newPeriod) {
        this.period = newPeriod;
        this.modificationDate = LocalDateTime.now();
        this.version++;
    }

    public void updateMetadata(String metaTitle, String metaDescription) {
        this.metaTitle = metaTitle;
        this.metaDescription = metaDescription;
        this.modificationDate = LocalDateTime.now();
        this.version++;
    }

    public void updatePriority(int priority) {
        this.priority = Math.max(0, priority);
        this.modificationDate = LocalDateTime.now();
        this.version++;
    }

    public void addImage(ImageCollection image) {
        if (image == null) {
            throw new IllegalArgumentException("Image cannot be null");
        }
        this.images.add(image);
        this.modificationDate = LocalDateTime.now();
        this.version++;
    }

    public void removeImage(String imageUrl) {
        this.images.removeIf(image -> image.getUrl().equals(imageUrl));
        this.modificationDate = LocalDateTime.now();
        this.version++;
    }

    public void addProduct(UUID productId) {
        if (productId == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        if (!this.productIds.contains(productId)) {
            this.productIds.add(productId);
            this.modificationDate = LocalDateTime.now();
            this.version++;
        }
    }

    public void removeProduct(UUID productId) {
        if (this.productIds.remove(productId)) {
            this.modificationDate = LocalDateTime.now();
            this.version++;
        }
    }

    public void addProducts(List<UUID> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return;
        }

        boolean modified = false;
        for (UUID productId : productIds) {
            if (productId != null && !this.productIds.contains(productId)) {
                this.productIds.add(productId);
                modified = true;
            }
        }

        if (modified) {
            this.modificationDate = LocalDateTime.now();
            this.version++;
        }
    }

    public void clearProducts() {
        if (!this.productIds.isEmpty()) {
            this.productIds.clear();
            this.modificationDate = LocalDateTime.now();
            this.version++;
        }
    }

    public void publish() {
        if (!this.active) {
            throw new IllegalStateException("Cannot publish an inactive collection");
        }
        if (this.period != null && !this.period.isCurrentlyActive()) {
            throw new IllegalStateException("Cannot publish collection outside its active period");
        }

        this.published = true;
        this.publicationDate = LocalDateTime.now();
        this.modificationDate = LocalDateTime.now();
        this.version++;
    }

    public void unpublish() {
        this.published = false;
        this.modificationDate = LocalDateTime.now();
        this.version++;
    }

    public void activate() {
        this.active = true;
        this.modificationDate = LocalDateTime.now();
        this.version++;
    }

    public void deactivate() {
        this.active = false;
        this.published = false; // Automatically unpublish when deactivating
        this.modificationDate = LocalDateTime.now();
        this.version++;
    }

    public void archive() {
        this.active = false;
        this.published = false;
        this.modificationDate = LocalDateTime.now();
        this.version++;
    }

    public boolean isVisible() {
        return this.active && this.published;
    }

    public boolean isCurrentlyActive() {
        if (!this.active) {
            return false;
        }

        if (this.period == null) {
            return true; // Permanent collection
        }

        return this.period.isCurrentlyActive();
    }

    public boolean hasProducts() {
        return !this.productIds.isEmpty();
    }

    public boolean containsProduct(UUID productId) {
        return this.productIds.contains(productId);
    }

    public int getProductCount() {
        return this.productIds.size();
    }

    public boolean isPermanent() {
        return this.period == null;
    }

    public boolean isSeasonal() {
        return this.period != null;
    }

    public boolean isExpired() {
        return this.period != null && this.period.isExpired();
    }

    public boolean isUpcoming() {
        return this.period != null && this.period.isUpcoming();
    }

    public ImageCollection getPrimaryImage() {
        return this.images.stream()
                .min((img1, img2) -> Integer.compare(img1.getDisplayOrder(), img2.getDisplayOrder()))
                .orElse(null);
    }

    public List<ImageCollection> getVisibleImages() {
        return this.images.stream()
                .filter(image -> image.getDisplayOrder() >= 0)
                .sorted((img1, img2) -> Integer.compare(img1.getDisplayOrder(), img2.getDisplayOrder()))
                .toList();
    }

    private String validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Collection name cannot be null or empty");
        }
        if (name.length() > 255) {
            throw new IllegalArgumentException("Collection name cannot exceed 255 characters");
        }
        return name.trim();
    }

    private String generateSlug(String providedSlug, String name) {
        if (providedSlug != null && !providedSlug.trim().isEmpty()) {
            return validateAndNormalizeSlug(providedSlug);
        }

        return name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }

    private String validateAndNormalizeSlug(String slug) {
        if (slug == null || slug.trim().isEmpty()) {
            throw new IllegalArgumentException("Slug cannot be null or empty");
        }

        String normalizedSlug = slug.toLowerCase().trim();
        if (!normalizedSlug.matches("^[a-z0-9-]+$")) {
            throw new IllegalArgumentException("Slug must contain only lowercase letters, numbers, and hyphens");
        }

        return normalizedSlug;
    }
}
