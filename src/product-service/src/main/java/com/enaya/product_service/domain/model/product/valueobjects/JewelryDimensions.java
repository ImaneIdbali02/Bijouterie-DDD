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
    BigDecimal length; // en mm
    
    @Column(name = "dimensions_width")
    BigDecimal width; // en mm
    
    @Column(name = "dimensions_height")
    BigDecimal height; // en mm
    
    @Column(name = "dimensions_weight")
    BigDecimal weight; // en grammes
    
    @Column(name = "dimensions_unit", length = 10)
    String unit; // mm, cm, inch

    private JewelryDimensions(BigDecimal length, BigDecimal width, BigDecimal height, BigDecimal weight, String unit) {
        this.length = validateDimension(length, "length");
        this.width = validateDimension(width, "width");
        this.height = validateDimension(height, "height");
        this.weight = validateWeight(weight);
        this.unit = validateUnit(unit);
    }

    public static JewelryDimensions of(double length, double width, double height, double weight) {
        return new JewelryDimensions(BigDecimal.valueOf(length), BigDecimal.valueOf(width), BigDecimal.valueOf(height), BigDecimal.valueOf(weight), "mm");
    }

    public static JewelryDimensions of(double length, double width, double height, double weight, String unit) {
        return new JewelryDimensions(BigDecimal.valueOf(length), BigDecimal.valueOf(width), BigDecimal.valueOf(height), BigDecimal.valueOf(weight), unit);
    }

    // Factory methods pour les types courants de bijoux
    public static JewelryDimensions ring(double diameter, double width, double weight) {
        BigDecimal bdDiameter = BigDecimal.valueOf(diameter);
        BigDecimal bdWidth = BigDecimal.valueOf(width);
        BigDecimal bdWeight = BigDecimal.valueOf(weight);
        return new JewelryDimensions(bdDiameter, bdWidth, bdWidth, bdWeight, "mm");
    }

    public static JewelryDimensions necklace(double length, double width, double weight) {
        return new JewelryDimensions(BigDecimal.valueOf(length), BigDecimal.valueOf(width), BigDecimal.ZERO, BigDecimal.valueOf(weight), "mm");
    }

    public static JewelryDimensions bracelet(double length, double width, double weight) {
        BigDecimal bdLength = BigDecimal.valueOf(length);
        BigDecimal bdWidth = BigDecimal.valueOf(width);
        BigDecimal bdWeight = BigDecimal.valueOf(weight);
        return new JewelryDimensions(bdLength, bdWidth, bdWidth, bdWeight, "mm");
    }

    public static JewelryDimensions earring(double length, double width, double weight) {
        BigDecimal bdLength = BigDecimal.valueOf(length);
        BigDecimal bdWidth = BigDecimal.valueOf(width);
        BigDecimal bdWeight = BigDecimal.valueOf(weight);
        return new JewelryDimensions(bdLength, bdWidth, bdWidth, bdWeight, "mm");
    }

    private BigDecimal validateDimension(BigDecimal dimension, String dimensionName) {
        if (dimension.signum() < 0) {
            throw new IllegalArgumentException(dimensionName + " cannot be negative");
        }
        if (dimension.compareTo(new BigDecimal("1000")) > 0) { // 1 mètre maximum
            throw new IllegalArgumentException(dimensionName + " cannot exceed 1000mm");
        }
        return dimension.setScale(2, RoundingMode.HALF_UP); // Arrondi à 2 décimales
    }

    private BigDecimal validateWeight(BigDecimal weight) {
        if (weight.signum() < 0) {
            throw new IllegalArgumentException("Weight cannot be negative");
        }
        if (weight.compareTo(new BigDecimal("1000")) > 0) { // 1 kg maximum
            throw new IllegalArgumentException("Weight cannot exceed 1000g");
        }
        return weight.setScale(2, RoundingMode.HALF_UP);
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

    public BigDecimal getVolume() {
        return length.multiply(width).multiply(height);
    }

    public BigDecimal getSurfaceArea() {
        BigDecimal lw = length.multiply(width);
        BigDecimal lh = length.multiply(height);
        BigDecimal wh = width.multiply(height);
        return new BigDecimal("2").multiply(lw.add(lh).add(wh));
    }

    public boolean isRing() {
        return length.subtract(width).abs().compareTo(new BigDecimal("0.1")) < 0 &&
               width.subtract(height).abs().compareTo(new BigDecimal("0.1")) < 0;
    }

    public boolean isNecklace() {
        return height.compareTo(BigDecimal.ZERO) == 0 && length.compareTo(width) > 0;
    }

    public boolean isBracelet() {
        return length.subtract(width).abs().compareTo(new BigDecimal("0.1")) > 0 &&
               width.subtract(height).abs().compareTo(new BigDecimal("0.1")) < 0;
    }

    public boolean isEarring() {
        return length.compareTo(width) > 0 &&
               width.subtract(height).abs().compareTo(new BigDecimal("0.1")) < 0;
    }

    public BigDecimal convertToUnit(String targetUnit) {
        if (unit.equals(targetUnit)) {
            return length;
        }
        BigDecimal conversionFactor;
        switch (unit) {
            case "mm":
                conversionFactor = switch (targetUnit) {
                    case "cm" -> new BigDecimal("0.1");
                    case "inch" -> new BigDecimal("1").divide(new BigDecimal("25.4"), 4, RoundingMode.HALF_UP);
                    default -> BigDecimal.ONE;
                };
                break;
            case "cm":
                conversionFactor = switch (targetUnit) {
                    case "mm" -> new BigDecimal("10");
                    case "inch" -> new BigDecimal("1").divide(new BigDecimal("2.54"), 4, RoundingMode.HALF_UP);
                    default -> BigDecimal.ONE;
                };
                break;
            case "inch":
                conversionFactor = switch (targetUnit) {
                    case "mm" -> new BigDecimal("25.4");
                    case "cm" -> new BigDecimal("2.54");
                    default -> BigDecimal.ONE;
                };
                break;
            default:
                conversionFactor = BigDecimal.ONE;
        }
        return length.multiply(conversionFactor).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String toString() {
        return String.format("%.2f x %.2f x %.2f %s (%.2fg)", 
            length, width, height, unit, weight);
    }
} 