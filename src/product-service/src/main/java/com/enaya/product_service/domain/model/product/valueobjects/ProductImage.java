package com.enaya.product_service.domain.model.product.valueobjects;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import lombok.Value;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

@Value
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class ProductImage {
    @Column(name = "url", nullable = false, length = 500)
    String url;
    
    @Column(name = "alt_text", length = 255)
    String altText;
    
    @Column(name = "display_order")
    int displayOrder;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "image_type", length = 20)
    ImageType type;
    
    Dimensions dimensions;

    private ProductImage(String url, String altText, int displayOrder, ImageType type, Dimensions dimensions) {
        this.url = validateUrl(url);
        this.altText = validateAltText(altText);
        this.displayOrder = validateDisplayOrder(displayOrder);
        this.type = type;
        this.dimensions = dimensions;
    }

    public static ProductImage of(String url, String altText, int displayOrder) {
        return new ProductImage(url, altText, displayOrder, ImageType.SECONDARY, null);
    }

    public static ProductImage of(String url, String altText, int displayOrder, ImageType type, Dimensions dimensions) {
        return new ProductImage(url, altText, displayOrder, type, dimensions);
    }

    // Factory methods for common image types
    public static ProductImage primary(String url, String altText) {
        return new ProductImage(url, altText, 0, ImageType.PRIMARY, null);
    }

    public static ProductImage thumbnail(String url, String altText, Dimensions dimensions) {
        return new ProductImage(url, altText, Integer.MAX_VALUE, ImageType.THUMBNAIL, dimensions);
    }

    public static ProductImage zoom(String url, String altText, Dimensions dimensions) {
        return new ProductImage(url, altText, 0, ImageType.ZOOM, dimensions);
    }

    private String validateUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalArgumentException("Image URL cannot be null or empty");
        }
        if (!url.matches("^https?://.+")) {
            throw new IllegalArgumentException("Image URL must be a valid HTTP(S) URL");
        }
        return url.trim();
    }

    private String validateAltText(String altText) {
        if (altText == null || altText.trim().isEmpty()) {
            throw new IllegalArgumentException("Alt text cannot be null or empty");
        }
        if (altText.length() > 255) {
            throw new IllegalArgumentException("Alt text cannot exceed 255 characters");
        }
        return altText.trim();
    }

    private int validateDisplayOrder(int displayOrder) {
        if (displayOrder < 0) {
            throw new IllegalArgumentException("Display order cannot be negative");
        }
        return displayOrder;
    }

    public boolean isPrimary() {
        return type == ImageType.PRIMARY;
    }

    public boolean isThumbnail() {
        return type == ImageType.THUMBNAIL;
    }

    public boolean isZoomable() {
        return type == ImageType.ZOOM;
    }

    public String getFileExtension() {
        if (url == null || !url.contains(".")) {
            return "";
        }
        return url.substring(url.lastIndexOf(".") + 1).toLowerCase();
    }

    public boolean isValidImageFormat() {
        String extension = getFileExtension();
        return extension.matches("^(jpg|jpeg|png|gif|webp|svg)$");
    }

    @Override
    public String toString() {
        return String.format("Image[url=%s, alt=%s, order=%d, type=%s]", url, altText, displayOrder, type);
    }

    public enum ImageType {
        PRIMARY,
        SECONDARY,
        THUMBNAIL,
        ZOOM
    }

    @Value
    @Embeddable
    @NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
    public static class Dimensions {
        @Column(name = "width")
        int width;
        
        @Column(name = "height")
        int height;

        private Dimensions(int width, int height) {
            this.width = validateDimension(width, "width");
            this.height = validateDimension(height, "height");
        }

        public static Dimensions of(int width, int height) {
            return new Dimensions(width, height);
        }

        private int validateDimension(int dimension, String dimensionName) {
            if (dimension <= 0) {
                throw new IllegalArgumentException(dimensionName + " must be positive");
            }
            return dimension;
        }

        public double getAspectRatio() {
            return (double) width / height;
        }

        public boolean isSquare() {
            return width == height;
        }

        public boolean isLandscape() {
            return width > height;
        }

        public boolean isPortrait() {
            return height > width;
        }
    }
}
