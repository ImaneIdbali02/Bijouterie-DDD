package com.enaya.product_service.infrastructure.messaging.listener;

import com.enaya.product_service.domain.event.category.CategoryCreated;
import com.enaya.product_service.infrastructure.persistence.outbox.OutboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class CategoryEventListener {

    private final OutboxService outboxService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCategoryCreatedEvent(CategoryCreated event) {
        outboxService.createAndSaveEvent(
                event.getCategoryId().toString(),
                "Category",
                "CategoryCreated",
                "categories",
                event
        );
    }
} 