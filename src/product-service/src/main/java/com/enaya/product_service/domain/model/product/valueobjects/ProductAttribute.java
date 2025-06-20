package com.enaya.product_service.domain.model.product.valueobjects;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import lombok.Value;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

@Value
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class ProductAttribute {
    @Column(name = "attribute_name")
    String name;
    
    @Column(name = "attribute_value")
    String value;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "attribute_type")
    AttributeType type;

    private ProductAttribute(String name, String value, AttributeType type) {
        this.name = validateName(name);
        this.value = validateValue(value);
        this.type = type;
    }

    public static ProductAttribute of(String name, String value) {
        return new ProductAttribute(name, value, AttributeType.STRING);
    }

    public static ProductAttribute of(String name, String value, AttributeType type) {
        return new ProductAttribute(name, value, type);
    }

    // Factory methods for common attributes
    public static ProductAttribute color(String value) {
        return new ProductAttribute("color", value, AttributeType.STRING);
    }

    public static ProductAttribute size(String value) {
        return new ProductAttribute("size", value, AttributeType.STRING);
    }

    public static ProductAttribute material(String value) {
        return new ProductAttribute("material", value, AttributeType.STRING);
    }

    public static ProductAttribute weight(String value) {
        return new ProductAttribute("weight", value, AttributeType.NUMBER);
    }

    public static ProductAttribute brand(String value) {
        return new ProductAttribute("brand", value, AttributeType.STRING);
    }

    private String validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Attribute name cannot be null or empty");
        }
        if (name.length() > 100) {
            throw new IllegalArgumentException("Attribute name cannot exceed 100 characters");
        }
        return name.trim().toLowerCase();
    }

    private String validateValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Attribute value cannot be null or empty");
        }
        if (value.length() > 255) {
            throw new IllegalArgumentException("Attribute value cannot exceed 255 characters");
        }
        return value.trim();
    }

    public boolean isNumeric() {
        return type == AttributeType.NUMBER;
    }

    public boolean isBoolean() {
        return type == AttributeType.BOOLEAN;
    }

    public Double getAsNumber() {
        if (!isNumeric()) {
            throw new IllegalStateException("Attribute is not numeric");
        }
        try {
            return Double.parseDouble(this.value);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Cannot parse value as number: " + this.value);
        }
    }

    public Boolean getAsBoolean() {
        if (!isBoolean()) {
            throw new IllegalStateException("Attribute is not boolean");
        }
        return Boolean.parseBoolean(this.value);
    }

    @Override
    public String toString() {
        return String.format("%s: %s (%s)", name, value, type);
    }

    public enum AttributeType {
        STRING,
        NUMBER,
        BOOLEAN,
        DATE
    }
}
