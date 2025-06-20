package com.enaya.product_service.domain.event.product;

import com.enaya.product_service.domain.model.product.valueobjects.ProductAttribute;
import com.enaya.product_service.domain.model.product.valueobjects.ProductImage;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ProductUpdated(
        UUID productId,
        String name,
        String description,
        BigDecimal price,
        String currency,
        UUID categoryId,
        LocalDateTime updatedAt,
        String sku,
        boolean active,
        List<UUID> collectionIds,
        List<ProductAttribute> attributes,
        List<ProductImage> images,
        Long version
) {
    public static ProductUpdated of(
            UUID productId,
            String name,
            String description,
            BigDecimal price,
            String currency,
            UUID categoryId,
            LocalDateTime updatedAt,
            String sku,
            boolean active,
            List<UUID> collectionIds,
            List<ProductAttribute> attributes,
            List<ProductImage> images,
            Long version) {
        return new ProductUpdated(
                productId,
                name,
                description,
                price,
                currency,
                categoryId,
                updatedAt,
                sku,
                active,
                collectionIds,
                attributes,
                images,
                version
        );
    }
}