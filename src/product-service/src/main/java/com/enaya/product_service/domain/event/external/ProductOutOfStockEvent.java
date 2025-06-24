package com.enaya.product_service.domain.event.external;

import java.time.LocalDateTime;

public class ProductOutOfStockEvent {
    private String stockItemId;
    private String productId;
    private String variant;
    private LocalDateTime timestamp;

    public String getStockItemId() { return stockItemId; }
    public void setStockItemId(String stockItemId) { this.stockItemId = stockItemId; }
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public String getVariant() { return variant; }
    public void setVariant(String variant) { this.variant = variant; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}