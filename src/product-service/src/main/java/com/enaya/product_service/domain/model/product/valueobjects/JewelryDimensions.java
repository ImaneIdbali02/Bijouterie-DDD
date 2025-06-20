package com.enaya.product_service.domain.model.product.valueobjects;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Column;
import lombok.Value;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

@Value
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class JewelryDimensions {
    @Column(name = "dimensions_length")
    double length; // en mm
    
    @Column(name = "dimensions_width")
    double width; // en mm
    
    @Column(name = "dimensions_height")
    double height; // en mm
    
    @Column(name = "dimensions_weight")
    double weight; // en grammes
    
    @Column(name = "dimensions_unit", length = 10)
    String unit; // mm, cm, inch

    private JewelryDimensions(double length, double width, double height, double weight, String unit) {
        this.length = validateDimension(length, "length");
        this.width = validateDimension(width, "width");
        this.height = validateDimension(height, "height");
        this.weight = validateWeight(weight);
        this.unit = validateUnit(unit);
    }

    public static JewelryDimensions of(double length, double width, double height, double weight) {
        return new JewelryDimensions(length, width, height, weight, "mm");
    }

    public static JewelryDimensions of(double length, double width, double height, double weight, String unit) {
        return new JewelryDimensions(length, width, height, weight, unit);
    }

    // Factory methods pour les types courants de bijoux
    public static JewelryDimensions ring(double diameter, double width, double weight) {
        return new JewelryDimensions(diameter, width, width, weight, "mm");
    }

    public static JewelryDimensions necklace(double length, double width, double weight) {
        return new JewelryDimensions(length, width, 0, weight, "mm");
    }

    public static JewelryDimensions bracelet(double length, double width, double weight) {
        return new JewelryDimensions(length, width, width, weight, "mm");
    }

    public static JewelryDimensions earring(double length, double width, double weight) {
        return new JewelryDimensions(length, width, width, weight, "mm");
    }

    private double validateDimension(double dimension, String dimensionName) {
        if (dimension < 0) {
            throw new IllegalArgumentException(dimensionName + " cannot be negative");
        }
        if (dimension > 1000) { // 1 mètre maximum
            throw new IllegalArgumentException(dimensionName + " cannot exceed 1000mm");
        }
        return Math.round(dimension * 100.0) / 100.0; // Arrondi à 2 décimales
    }

    private double validateWeight(double weight) {
        if (weight < 0) {
            throw new IllegalArgumentException("Weight cannot be negative");
        }
        if (weight > 1000) { // 1 kg maximum
            throw new IllegalArgumentException("Weight cannot exceed 1000g");
        }
        return Math.round(weight * 100.0) / 100.0; // Arrondi à 2 décimales
    }

    private String validateUnit(String unit) {
        if (unit == null || unit.trim().isEmpty()) {
            return "mm";
        }
        String normalizedUnit = unit.toLowerCase().trim();
        if (!normalizedUnit.matches("^(mm|cm|inch)$")) {
            throw new IllegalArgumentException("Unit must be one of: mm, cm, inch");
        }
        return normalizedUnit;
    }

    public double getVolume() {
        return length * width * height;
    }

    public double getSurfaceArea() {
        return 2 * (length * width + length * height + width * height);
    }

    public boolean isRing() {
        return Math.abs(length - width) < 0.1 && Math.abs(width - height) < 0.1;
    }

    public boolean isNecklace() {
        return height == 0 && length > width;
    }

    public boolean isBracelet() {
        return Math.abs(length - width) > 0.1 && Math.abs(width - height) < 0.1;
    }

    public boolean isEarring() {
        return length > width && Math.abs(width - height) < 0.1;
    }

    public double convertToUnit(String targetUnit) {
        if (unit.equals(targetUnit)) {
            return length;
        }
        return switch (unit) {
            case "mm" -> switch (targetUnit) {
                case "cm" -> length / 10;
                case "inch" -> length / 25.4;
                default -> length;
            };
            case "cm" -> switch (targetUnit) {
                case "mm" -> length * 10;
                case "inch" -> length / 2.54;
                default -> length;
            };
            case "inch" -> switch (targetUnit) {
                case "mm" -> length * 25.4;
                case "cm" -> length * 2.54;
                default -> length;
            };
            default -> length;
        };
    }

    @Override
    public String toString() {
        return String.format("%.2f x %.2f x %.2f %s (%.2fg)", 
            length, width, height, unit, weight);
    }
} 