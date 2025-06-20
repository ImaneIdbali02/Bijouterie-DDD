package com.enaya.product_service.domain.event;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class CollectionArchived {
    private final UUID eventId = UUID.randomUUID();
    private final LocalDateTime occurredOn = LocalDateTime.now();
    private final UUID collectionId;

    public static CollectionArchived from(UUID collectionId) {
        return new CollectionArchived(collectionId);
    }
}
