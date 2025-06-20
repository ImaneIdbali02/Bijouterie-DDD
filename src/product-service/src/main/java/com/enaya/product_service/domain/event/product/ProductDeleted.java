package com.enaya.product_service.domain.event.product;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProductDeleted(
        UUID productId,
        LocalDateTime deletedAt
) {
    public static ProductDeleted of(
            UUID productId,
            LocalDateTime deletedAt) {
        return new ProductDeleted(
                productId,
                deletedAt
        );
    }
}