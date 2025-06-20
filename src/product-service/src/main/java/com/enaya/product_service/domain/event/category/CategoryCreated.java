package com.enaya.product_service.domain.event;

import com.enaya.product_service.domain.model.category.Category;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class CategoryCreated {
    private final UUID eventId = UUID.randomUUID();
    private final LocalDateTime occurredOn = LocalDateTime.now();
    private final UUID categoryId;
    private final String categoryName;

    public static CategoryCreated from(Category category) {
        return new CategoryCreated(category.getId(), category.getName());
    }
}
