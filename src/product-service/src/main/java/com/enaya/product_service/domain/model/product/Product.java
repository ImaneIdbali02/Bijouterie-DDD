package com.enaya.product_service.domain.model.product;

import com.enaya.product_service.domain.model.collection.Collection;
import com.enaya.product_service.domain.model.product.valueobjects.*;
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
@Table(name = "products")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(name = "sku", nullable = false, unique = true, length = 100)
    private String sku;

    @Embedded
    private Price price;

    @Column(name = "category_id")
    private UUID categoryId;

    @ManyToMany
    @JoinTable(
        name = "product_collections",
        joinColumns = @JoinColumn(name = "product_id"),
        inverseJoinColumns = @JoinColumn(name = "collection_id")
    )
    private List<Collection> collections = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ProductVariant> variants = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "product_attributes", joinColumns = @JoinColumn(name = "product_id"))
    private List<ProductAttribute> attributes = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "product_id"))
    private List<ProductImage> images = new ArrayList<>();

    @Column(nullable = false)
    private boolean active;

    @CreationTimestamp
    @Column(name = "creation_date", nullable = false, updatable = false)
    private LocalDateTime creationDate;

    @UpdateTimestamp
    @Column(name = "modification_date", nullable = false)
    private LocalDateTime modificationDate;

    @Version
    private Long version;

    @Builder
    private Product(UUID id, String name, String description, String sku, Price price,
                   UUID categoryId, List<Collection> collections, List<ProductVariant> variants,
                   List<ProductAttribute> attributes, List<ProductImage> images, boolean active) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.sku = sku;
        this.price = price;
        this.categoryId = categoryId;
        this.collections = collections != null ? new ArrayList<>(collections) : new ArrayList<>();
        this.variants = variants != null ? new ArrayList<>(variants) : new ArrayList<>();
        this.attributes = attributes != null ? new ArrayList<>(attributes) : new ArrayList<>();
        this.images = images != null ? new ArrayList<>(images) : new ArrayList<>();
        this.active = active;
        this.creationDate = LocalDateTime.now();
        this.modificationDate = LocalDateTime.now();
        this.version = 0L;
    }

    public static Product create(String name, String description, String sku, Price price, UUID categoryId) {
        return Product.builder()
                .name(name)
                .description(description)
                .sku(sku)
                .price(price)
                .categoryId(categoryId)
                .active(true)
                .build();
    }

    public void updateBasicInfo(String name, String description) {
        this.name = name;
        this.description = description;
        this.modificationDate = LocalDateTime.now();
        this.version++;
    }

    public void updatePrice(Price newPrice) {
        if (newPrice == null) {
            throw new IllegalArgumentException("Price cannot be null");
        }
        this.price = newPrice;
        this.modificationDate = LocalDateTime.now();
        this.version++;
    }

    public void addVariant(ProductVariant variant) {
        if (variant == null) {
            throw new IllegalArgumentException("Variant cannot be null");
        }
        if (variant.getProduct() != this) {
            variant.setProduct(this);
        }
        validateVariantPrice(variant.getPrice());
        this.variants.add(variant);
        this.modificationDate = LocalDateTime.now();
        this.version++;
    }

    public void removeVariant(UUID variantId) {
        this.variants.removeIf(variant -> variant.getId().equals(variantId));
        this.modificationDate = LocalDateTime.now();
        this.version++;
    }

    public void addAttribute(ProductAttribute attribute) {
        if (attribute == null) {
            throw new IllegalArgumentException("Attribute cannot be null");
        }
        this.attributes.removeIf(attr -> attr.getName().equals(attribute.getName()));
        this.attributes.add(attribute);
        this.modificationDate = LocalDateTime.now();
        this.version++;
    }

    public void addImage(ProductImage image) {
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

    public void addToCollection(Collection collection) {
        if (collection == null) {
            throw new IllegalArgumentException("Collection cannot be null");
        }
        if (!this.collections.contains(collection)) {
            this.collections.add(collection);
            this.modificationDate = LocalDateTime.now();
            this.version++;
        }
    }

    public void removeFromCollection(Collection collection) {
        if (this.collections.remove(collection)) {
            this.modificationDate = LocalDateTime.now();
            this.version++;
        }
    }

    public boolean isInCollection(Collection collection) {
        return this.collections.contains(collection);
    }

    public void activate() {
        this.active = true;
        this.modificationDate = LocalDateTime.now();
        this.version++;
    }

    public void deactivate() {
        this.active = false;
        this.modificationDate = LocalDateTime.now();
        this.version++;
    }

    public void changeCategory(UUID newCategoryId) {
        if (newCategoryId == null) {
            throw new IllegalArgumentException("Category ID cannot be null");
        }
        this.categoryId = newCategoryId;
        this.modificationDate = LocalDateTime.now();
        this.version++;
    }

    public boolean hasVariants() {
        return !this.variants.isEmpty();
    }

    public ProductImage getPrimaryImage() {
        return this.images.stream()
                .min((img1, img2) -> Integer.compare(img1.getDisplayOrder(), img2.getDisplayOrder()))
                .orElse(null);
    }

    private void validateVariantPrice(Price variantPrice) {
        if (variantPrice.getAmount().compareTo(this.price.getAmount()) < 0) {
            throw new IllegalArgumentException("Variant price cannot be lower than base product price");
        }
    }
}