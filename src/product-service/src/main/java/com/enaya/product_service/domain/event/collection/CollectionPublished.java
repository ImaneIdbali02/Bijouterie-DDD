package com.enaya.product_service.domain.event.collection;

import com.enaya.product_service.domain.model.collection.Collection;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class CollectionPublished {
    private final UUID eventId = UUID.randomUUID();
    private final LocalDateTime occurredOn = LocalDateTime.now();
    private final UUID collectionId;
    private final String collectionName;

    public static CollectionPublished from(Collection collection) {
        return new CollectionPublished(collection.getId(), collection.getName());
    }
}
