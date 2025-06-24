package com.enaya.product_service.domain.model.collection.valueobjects;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Column;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class ImageCollection {
    @Column(name = "url", nullable = false, length = 500)
    String url;
    
    @Column(name = "alt_text", length = 255)
    String altText;
    
    @Column(name = "display_order")
    int displayOrder;
    
    @Column(name = "image_type", length = 20)
    String type; // BANNER, THUMBNAIL, HERO, BACKGROUND
    
    @Column(name = "file_size_bytes")
    long fileSizeBytes;
    
    @Column(name = "width")
    int width;
    
    @Column(name = "height")
    int height;
    
    @Column(name = "description")
    String description;

    private ImageCollection(String url, String altText, int displayOrder, String type,
                            long fileSizeBytes, int width, int height, String description) {
        this.url = validateUrl(url);
        this.altText = validateAltText(altText);
        this.displayOrder = validateDisplayOrder(displayOrder);
        this.type = validateType(type);
        this.fileSizeBytes = validateFileSizeBytes(fileSizeBytes);
        this.width = validateDimension(width, "width");
        this.height = validateDimension(height, "height");
        this.description = description;
    }

    public static ImageCollection of(String url, String altText, int displayOrder) {
        return new ImageCollection(url, altText, displayOrder, "THUMBNAIL", 0, 0, 0, null);
    }

    public static ImageCollection banner(String url, String altText) {
        return new ImageCollection(url, altText, 0, "BANNER", 0, 0, 0, null);
    }

    public static ImageCollection hero(String url, String altText, String description) {
        return new ImageCollection(url, altText, 0, "HERO", 0, 0, 0, description);
    }

    public static ImageCollection background(String url, String altText) {
        return new ImageCollection(url, altText, Integer.MAX_VALUE, "BACKGROUND", 0, 0, 0, null);
    }

    public static ImageCollection thumbnail(String url, String altText, int width, int height) {
        return new ImageCollection(url, altText, 1, "THUMBNAIL", 0, width, height, null);
    }

    public static ImageCollection withFullDetails(String url, String altText, int displayOrder,
                                                  String type, long fileSizeBytes, int width,
                                                  int height, String description) {
        return new ImageCollection(url, altText, displayOrder, type, fileSizeBytes,
                width, height, description);
    }

    public boolean isBanner() {
        return "BANNER".equals(this.type);
    }

    public boolean isHero() {
        return "HERO".equals(this.type);
    }

    public boolean isThumbnail() {
        return "THUMBNAIL".equals(this.type);
    }

    public boolean isBackground() {
        return "BACKGROUND".equals(this.type);
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

    public boolean isWidescreen() {
        double ratio = getAspectRatio();
        return ratio >= 1.5; // 3:2 or wider
    }

    public String getFormattedSize() {
        if (width <= 0 || height <= 0) {
            return "Unknown";
        }
        return String.format("%dx%d", width, height);
    }

    public String getFormattedFileSize() {
        if (fileSizeBytes <= 0) {
            return "Unknown";
        }

        if (fileSizeBytes < 1024) {
            return fileSizeBytes + " B";
        } else if (fileSizeBytes < 1024 * 1024) {
            return String.format("%.1f KB", fileSizeBytes / 1024.0);
        } else {
            return String.format("%.1f MB", fileSizeBytes / (1024.0 * 1024.0));
        }
    }

    @Override
    public String toString() {
        return String.format("ImageCollection{url='%s', type='%s', order=%d, size=%s}",
                url, type, displayOrder, getFormattedSize());
    }

    private String validateUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalArgumentException("Image URL cannot be null or empty");
        }
        return url.trim();
    }

    private String validateAltText(String altText) {
        if (altText == null) {
            return "";
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

    private String validateType(String type) {
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("Image type cannot be null or empty");
        }
        String normalizedType = type.toUpperCase().trim();
        if (!normalizedType.matches("^(BANNER|THUMBNAIL|HERO|BACKGROUND)$")) {
            throw new IllegalArgumentException("Image type must be one of: BANNER, THUMBNAIL, HERO, BACKGROUND");
        }
        return normalizedType;
    }

    private long validateFileSizeBytes(long fileSizeBytes) {
        if (fileSizeBytes < 0) {
            throw new IllegalArgumentException("File size cannot be negative");
        }
        return fileSizeBytes;
    }

    private int validateDimension(int dimension, String dimensionName) {
        if (dimension < 0) {
            throw new IllegalArgumentException(dimensionName + " cannot be negative");
        }
        return dimension;
    }
}
