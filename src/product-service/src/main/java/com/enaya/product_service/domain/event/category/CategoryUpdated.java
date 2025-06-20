package com.enaya.product_service.domain.event.category;

import com.enaya.product_service.domain.model.category.Category;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class CategoryUpdated {
    private final UUID eventId = UUID.randomUUID();
    private final LocalDateTime occurredOn = LocalDateTime.now();
    private final UUID categoryId;
    private final String categoryName;

    public static CategoryUpdated from(Category category) {
        return new CategoryUpdated(category.getId(), category.getName());
    }
}
