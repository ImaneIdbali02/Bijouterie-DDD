package com.enaya.product_service.infrastructure.messaging.listener;


import com.enaya.product_service.application.service.ProductApplicationService;
import com.enaya.product_service.domain.event.external.ProductOutOfStockEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductOutOfStockEventListener {

    private final ProductApplicationService productApplicationService;

    @KafkaListener(
            topics = "${kafka.topics.stock-updates}",
            groupId = "${kafka.consumer.group-id}"
    )
    public void handleOutOfStock(ProductOutOfStockEvent event) {
        log.info("Received out of stock event for product: {}, variant: {}",
                event.productId(), event.variantId());

        try {
            productApplicationService.handleOutOfStock(event.productId(), event.variantId());
        } catch (Exception e) {
            log.error("Error processing out of stock event: {}", e.getMessage(), e);
        }
    }
}