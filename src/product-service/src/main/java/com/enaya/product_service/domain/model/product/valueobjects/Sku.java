package com.enaya.product_service.domain.model.product.valueobjects;

import jakarta.persistence.Embeddable;
import lombok.Value;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

@Value
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class Sku {
    String value;

    private Sku(String value) {
        this.value = validate(value);
    }

    public static Sku of(String value) {
        return new Sku(value);
    }

    private String validate(String sku) {
        if (sku == null || sku.trim().isEmpty()) {
            throw new IllegalArgumentException("SKU cannot be null or empty");
        }
        if (!sku.matches("^[A-Z0-9-_]+$")) {
            throw new IllegalArgumentException("SKU must contain only uppercase letters, numbers, hyphens and underscores");
        }
        return sku.trim();
    }

    @Override
    public String toString() {
        return value;
    }
} 