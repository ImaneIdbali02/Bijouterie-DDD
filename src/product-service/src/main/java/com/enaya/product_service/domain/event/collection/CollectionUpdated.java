package com.enaya.product_service.domain.event;

import com.enaya.product_service.domain.model.collection.Collection;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class CollectionUpdated {
    private final UUID eventId = UUID.randomUUID();
    private final LocalDateTime occurredOn = LocalDateTime.now();
    private final UUID collectionId;
    private final String collectionName;

    public static CollectionUpdated from(Collection collection) {
        return new CollectionUpdated(collection.getId(), collection.getName());
    }
}
