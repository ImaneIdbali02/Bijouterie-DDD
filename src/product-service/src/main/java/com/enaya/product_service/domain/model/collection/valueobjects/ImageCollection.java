package com.ecommerce.catalog.domain.collection.valueobject;

import lombok.Value;
import javax.validation.constraints.NotBlank;

@Value
public class ImageCollection {
    @NotBlank
    String url;
    int displayOrder;
    String altText;

    public ImageCollection(String url, int displayOrder, String altText) {
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalArgumentException("Image URL cannot be null or empty");
        }
        if (displayOrder < 0) {
            throw new IllegalArgumentException("Display order cannot be negative");
        }
        this.url = url.trim();
        this.displayOrder = displayOrder;
        this.altText = altText != null ? altText.trim() : "";
    }
}