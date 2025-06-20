package com.enaya.product_service.domain.event;

import com.enaya.product_service.domain.model.product.Product;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class ProductUpdated {
    private final UUID eventId = UUID.randomUUID();
    private final LocalDateTime occurredOn = LocalDateTime.now();
    private final UUID productId;
    private final String productName;

    public static ProductUpdated from(Product product) {
        return new ProductUpdated(product.getId(), product.getName());
    }
}
