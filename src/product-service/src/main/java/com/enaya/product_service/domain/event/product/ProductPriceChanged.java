package com.enaya.product_service.domain.event.product;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ProductPriceChanged(
        UUID productId,
        BigDecimal oldPrice,
        BigDecimal newPrice,
        String currency,
        LocalDateTime changedAt
) {
    public static ProductPriceChanged of(
            UUID productId,
            BigDecimal oldPrice,
            BigDecimal newPrice,
            String currency,
            LocalDateTime changedAt) {
        return new ProductPriceChanged(
                productId,
                oldPrice,
                newPrice,
                currency,
                changedAt
        );
    }
}