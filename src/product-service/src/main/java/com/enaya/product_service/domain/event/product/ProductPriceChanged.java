package com.enaya.product_service.domain.event;

import com.enaya.product_service.domain.model.product.valueobjects.Price;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class ProductPriceChanged {
    private final UUID eventId = UUID.randomUUID();
    private final LocalDateTime occurredOn = LocalDateTime.now();
    private final UUID productId;
    private final Price oldPrice;
    private final Price newPrice;

    public static ProductPriceChanged from(UUID productId, Price oldPrice, Price newPrice) {
        return new ProductPriceChanged(productId, oldPrice, newPrice);
    }
}
