package com.enaya.product_service.domain.model.product.valueobjects;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductImage {

    private String url;
    private String altText;
    private int displayOrder;
    private String type; // PRIMARY, SECONDARY, THUMBNAIL, ZOOM
    private long sizeBytes;
    private int width;
    private int height;

    private ProductImage(String url, String altText, int displayOrder, String type,
                         long sizeBytes, int width, int height) {
        this.url = validateUrl(url);
        this.altText = validateAltText(altText);
        this.displayOrder = validateDisplayOrder(displayOrder);
        this.type = validateType(type);
        this.sizeBytes = validateSizeBytes(sizeBytes);
        this.width = validateDimension(width, "width");
        this.height = validateDimension(height, "height");
    }

    public static ProductImage of(String url, String altText, int displayOrder) {
        return new ProductImage(url, altText, displayOrder, "SECONDARY", 0, 0, 0);
    }

    public static ProductImage primary(String url, String altText) {
        return new ProductImage(url, altText, 0, "PRIMARY", 0, 0, 0);
    }

    public static ProductImage thumbnail(String url, String altText, int width, int height) {
        return new ProductImage(url, altText, Integer.MAX_VALUE, "THUMBNAIL", 0, width, height);
    }

    public static ProductImage withFullDetails(String url, String altText, int displayOrder,
                                               String type, long sizeBytes, int width, int height) {
        return new ProductImage(url, altText, displayOrder, type, sizeBytes, width, height);
    }

    public boolean isPrimary() {
        return "PRIMARY".equals(this.type);
    }

    public boolean isThumbnail() {
        return "THUMBNAIL".equals(this.type);
    }

    public boolean isZoomable() {
        return "ZOOM".equals(this.type);
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

    public double getAspectRatio() {
        if (width <= 0 || height <= 0) {
            return 0.0;
        }
        return (double) width / height;
    }

    public boolean isSquare() {
        return width == height && width > 0;
    }

    public boolean isLandscape() {
        return width > height && height > 0;
    }

    public boolean isPortrait() {
        return height > width && width > 0;
    }

    @Override
    public String toString() {
        return String.format("ProductImage{url='%s', altText='%s', displayOrder=%d, type='%s', sizeBytes=%d, width=%d, height=%d}",
                url, altText, displayOrder, type, sizeBytes, width, height);
    }

    // Validation Methods

    private String validateUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("URL cannot be null or empty");
        }
        // Basic URL format check (optional, improve as needed)
        if (!url.matches("^(https?://|ftp://).+")) {
            throw new IllegalArgumentException("URL must start with http://, https://, or ftp://");
        }
        return url;
    }

    private String validateAltText(String altText) {
        if (altText == null || altText.isBlank()) {
            throw new IllegalArgumentException("Alt text cannot be null or empty");
        }
        if (altText.length() > 255) {
            throw new IllegalArgumentException("Alt text length must not exceed 255 characters");
        }
        return altText;
    }

    private int validateDisplayOrder(int displayOrder) {
        if (displayOrder < 0) {
            throw new IllegalArgumentException("Display order must be zero or positive");
        }
        return displayOrder;
    }

    private String validateType(String type) {
        if (type == null) {
            throw new IllegalArgumentException("Type cannot be null");
        }
        switch (type.toUpperCase()) {
            case "PRIMARY":
            case "SECONDARY":
            case "THUMBNAIL":
            case "ZOOM":
                return type.toUpperCase();
            default:
                throw new IllegalArgumentException("Invalid image type: " + type);
        }
    }

    private long validateSizeBytes(long sizeBytes) {
        if (sizeBytes < 0) {
            throw new IllegalArgumentException("Size in bytes cannot be negative");
        }
        return sizeBytes;
    }

    private int validateDimension(int dimension, String dimensionName) {
        if (dimension < 0) {
            throw new IllegalArgumentException(dimensionName + " cannot be negative");
        }
        return dimension;
    }
}
