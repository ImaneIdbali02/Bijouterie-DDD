package com.enaya.product_service.domain.model.product;

import com.enaya.product_service.domain.model.product.valueobjects.*;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "product_variants")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "sku", nullable = false, unique = true, length = 100)
    private String sku;

    @Embedded
    private Price price;

    @Embedded
    private JewelryDimensions dimensions;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "product_variant_attributes", joinColumns = @JoinColumn(name = "variant_id"))
    private List<ProductAttribute> specificAttributes = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "product_variant_images", joinColumns = @JoinColumn(name = "variant_id"))
    private List<ProductImage> images = new ArrayList<>();

    @Column(nullable = false)
    private boolean active;

    @Enumerated(EnumType.STRING)
    @Column(name = "stock_status", nullable = false)
    private StockStatus stockStatus = StockStatus.IN_STOCK;

    @Column(name = "stock_quantity")
    private Integer stockQuantity;

    @Column(name = "rating")
    private Double rating;

    @Column(name = "review_count")
    private Integer reviewCount = 0;

    @CreationTimestamp
    @Column(name = "creation_date", nullable = false, updatable = false)
    private LocalDateTime creationDate;

    @UpdateTimestamp
    @Column(name = "modification_date", nullable = false)
    private LocalDateTime modificationDate;

    @Version
    private Long version;

    public enum StockStatus {
        IN_STOCK, OUT_OF_STOCK, LOW_STOCK, DISCONTINUED
    }

    @Builder
    private ProductVariant(UUID id, Product product, String name, String sku, Price price,
                          JewelryDimensions dimensions, List<ProductAttribute> specificAttributes,
                          List<ProductImage> images, boolean active, StockStatus stockStatus,
                          Integer stockQuantity, Double rating, Integer reviewCount) {
        this.id = id;
        this.product = product;
        this.name = name;
        this.sku = sku;
        this.price = price;
        this.dimensions = dimensions;
        this.specificAttributes = specificAttributes != null ? new ArrayList<>(specificAttributes) : new ArrayList<>();
        this.images = images != null ? new ArrayList<>(images) : new ArrayList<>();
        this.active = active;
        this.stockStatus = stockStatus != null ? stockStatus : StockStatus.IN_STOCK;
        this.stockQuantity = stockQuantity;
        this.rating = rating;
        this.reviewCount = reviewCount != null ? reviewCount : 0;
        this.creationDate = LocalDateTime.now();
        this.modificationDate = LocalDateTime.now();
        this.version = 0L;
    }

    public static ProductVariant create(Product product, String name, String sku, Price price) {
        return ProductVariant.builder()
                .product(product)
                .name(name)
                .sku(sku)
                .price(price)
                .active(true)
                .stockStatus(StockStatus.IN_STOCK)
                .build();
    }

    public void updateBasicInfo(String name) {
        this.name = name;
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

    public void updateStockStatus(StockStatus status) {
        this.stockStatus = status;
        this.modificationDate = LocalDateTime.now();
        this.version++;
    }

    public void updateStockQuantity(Integer quantity) {
        this.stockQuantity = quantity;
        if (quantity != null && quantity <= 0) {
            this.stockStatus = StockStatus.OUT_OF_STOCK;
        } else if (quantity != null && quantity < 10) {
            this.stockStatus = StockStatus.LOW_STOCK;
        } else if (quantity != null && quantity >= 10) {
            this.stockStatus = StockStatus.IN_STOCK;
        }
        this.modificationDate = LocalDateTime.now();
        this.version++;
    }

    public void updateRating(Double rating) {
        if (rating != null && (rating < 0.0 || rating > 5.0)) {
            throw new IllegalArgumentException("Rating must be between 0.0 and 5.0");
        }
        this.rating = rating;
        this.modificationDate = LocalDateTime.now();
        this.version++;
    }

    public void incrementReviewCount() {
        this.reviewCount++;
        this.modificationDate = LocalDateTime.now();
        this.version++;
    }

    public void addSpecificAttribute(ProductAttribute attribute) {
        if (attribute == null) {
            throw new IllegalArgumentException("Attribute cannot be null");
        }
        this.specificAttributes.removeIf(attr -> attr.getName().equals(attribute.getName()));
        this.specificAttributes.add(attribute);
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

    public boolean isInStock() {
        return this.stockStatus == StockStatus.IN_STOCK || 
               (this.stockStatus == StockStatus.LOW_STOCK && this.stockQuantity != null && this.stockQuantity > 0);
    }

    public boolean isOutOfStock() {
        return this.stockStatus == StockStatus.OUT_OF_STOCK || 
               (this.stockQuantity != null && this.stockQuantity <= 0);
    }

    public ProductImage getPrimaryImage() {
        return this.images.stream()
                .min((img1, img2) -> Integer.compare(img1.getDisplayOrder(), img2.getDisplayOrder()))
                .orElse(null);
    }
}
