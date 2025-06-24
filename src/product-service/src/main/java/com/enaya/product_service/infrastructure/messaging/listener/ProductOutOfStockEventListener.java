package com.enaya.product_service.infrastructure.messaging.listener;

import com.enaya.product_service.application.service.ProductApplicationService;
import com.enaya.product_service.domain.event.external.ProductOutOfStockEvent;
import com.enaya.product_service.infrastructure.persistence.outbox.OutboxEvent;
import com.enaya.product_service.infrastructure.persistence.outbox.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductOutOfStockEventListener {

    private final ProductApplicationService productApplicationService;
    private final OutboxEventRepository outboxEventRepository;

    @KafkaListener(
            topics = "out-of-stock-detected",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    @Transactional
    public void handleOutOfStock(ProductOutOfStockEvent event) {
        log.info("Received out of stock event for product: {}, variant: {}",
                event.getProductId(), event.getVariant());

        try {
            // Save event to outbox
            OutboxEvent outboxEvent = new OutboxEvent(
                    "STOCK_OUT_OF_STOCK",
                    event.toString(),
                    "out-of-stock-detected",
                    event.getProductId(),
                    "Product"
            );
            outboxEventRepository.save(outboxEvent);

            // Convert productId and variant to UUID
            java.util.UUID productId = java.util.UUID.fromString(event.getProductId());
            java.util.UUID variantId = null;
            try {
                variantId = java.util.UUID.fromString(event.getVariant());
            } catch (Exception e) {
                log.warn("Variant ID is not a valid UUID: {}", event.getVariant());
            }

            // Process the event
            productApplicationService.handleOutOfStock(productId, variantId);

        } catch (Exception e) {
            log.error("Error processing out of stock event: {}", e.getMessage(), e);
        }
    }
}