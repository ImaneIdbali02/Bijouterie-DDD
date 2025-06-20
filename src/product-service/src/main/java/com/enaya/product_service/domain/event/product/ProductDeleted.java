package com.enaya.product_service.domain.event;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class ProductDeleted {
    private final UUID eventId = UUID.randomUUID();
    private final LocalDateTime occurredOn = LocalDateTime.now();
    private final UUID productId;

    public static ProductDeleted from(UUID productId) {
        return new ProductDeleted(productId);
    }
}
