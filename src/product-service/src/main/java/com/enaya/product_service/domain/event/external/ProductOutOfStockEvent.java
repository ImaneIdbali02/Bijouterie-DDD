package com.enaya.product_service.domain.event.external;

import java.util.UUID;

public record ProductOutOfStockEvent(
        UUID productId,
        UUID variantId
) {}