package com.enaya.product_service.application.event;

import com.enaya.product_service.domain.event.product.*;
import com.enaya.product_service.domain.model.product.Product;
import com.enaya.product_service.domain.model.product.valueobjects.Price;
import com.enaya.product_service.domain.model.collection.Collection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    public void publishProductCreated(Product product) {
        log.info("Publishing ProductCreated event for product: {}", product.getId());
        eventPublisher.publishEvent(ProductCreated.of(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice().getAmount(),
                String.valueOf(product.getPrice().getCurrency()),
                product.getCategoryId(),
                product.getCreationDate(),
                product.getSku(),
                product.isActive(),
                product.getCollections().stream().map(Collection::getId).toList(),
                product.getAttributes(),
                product.getImages(),
                product.getVersion()
        ));
    }

    public void publishProductUpdated(Product product) {
        log.info("Publishing ProductUpdated event for product: {}", product.getId());
        eventPublisher.publishEvent(ProductUpdated.of(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice().getAmount(),
                String.valueOf(product.getPrice().getCurrency()),
                product.getCategoryId(),
                product.getModificationDate(),
                product.getSku(),
                product.isActive(),
                product.getCollections().stream().map(Collection::getId).toList(),
                product.getAttributes(),
                product.getImages(),
                product.getVersion()
        ));
    }

    public void publishProductDeleted(UUID productId) {
        log.info("Publishing ProductDeleted event for product: {}", productId);
        eventPublisher.publishEvent(ProductDeleted.of(
                productId,
                LocalDateTime.now()
        ));
    }

    public void publishProductPriceChanged(UUID productId, Price oldPrice, Price newPrice) {
        log.info("Publishing ProductPriceChanged event for product: {}", productId);
        eventPublisher.publishEvent(ProductPriceChanged.of(
                productId,
                oldPrice.getAmount(),
                newPrice.getAmount(),
                String.valueOf(oldPrice.getCurrency()),
                LocalDateTime.now()
        ));
    }
}