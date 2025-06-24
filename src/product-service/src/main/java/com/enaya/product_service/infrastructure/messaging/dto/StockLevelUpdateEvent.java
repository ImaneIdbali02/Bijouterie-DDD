package com.enaya.product_service.infrastructure.messaging.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StockLevelUpdateEvent {
    private String productId;
    private String variantId;
    private String sku;
    private String newStatus;
} 