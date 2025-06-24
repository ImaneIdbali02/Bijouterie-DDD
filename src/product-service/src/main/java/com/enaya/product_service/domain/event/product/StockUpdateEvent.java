package com.enaya.product_service.domain.event.product;

import java.time.LocalDateTime;
import java.util.UUID;

public record StockUpdateEvent(
        UUID productId,
        UUID variantId,
        String status,
        LocalDateTime occurredAt
) {}