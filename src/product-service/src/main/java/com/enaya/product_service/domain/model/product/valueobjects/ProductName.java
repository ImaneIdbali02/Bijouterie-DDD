package com.enaya.product_service.domain.model.product.valueobjects;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Column;
import lombok.Value;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

@Value
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@JsonSerialize(using = ToStringSerializer.class)
public class ProductName {
    @Column(name = "name_value")
    String value;

    private ProductName(String value) {
        this.value = validate(value);
    }

    public static ProductName of(String value) {
        return new ProductName(value);
    }

    private String validate(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be null or empty");
        }
        if (name.length() > 255) {
            throw new IllegalArgumentException("Product name cannot exceed 255 characters");
        }
        return name.trim();
    }

    @Override
    public String toString() {
        return value;
    }
} 