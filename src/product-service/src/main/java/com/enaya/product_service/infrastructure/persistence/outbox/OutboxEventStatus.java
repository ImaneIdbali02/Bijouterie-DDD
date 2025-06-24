package com.enaya.product_service.infrastructure.persistence.outbox;

public enum OutboxEventStatus {
    PENDING,    // Événement en attente de publication
    PUBLISHED,  // Événement publié avec succès
    FAILED      // Événement en échec de publication
}
