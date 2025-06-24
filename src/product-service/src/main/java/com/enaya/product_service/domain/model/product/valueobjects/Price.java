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
public class Price {
    private static final String MAD_CURRENCY_CODE = "MAD";
    private static final Currency MAD_CURRENCY = Currency.getInstance(MAD_CURRENCY_CODE);
    
    @Column(name = "price_amount", nullable = false, precision = 10, scale = 2)
    BigDecimal amount;
    
    @Column(name = "price_currency", nullable = false, length = 3)
    Currency currency;

    private Price(BigDecimal amount, Currency currency) {
        this.amount = validateAmount(amount);
        this.currency = validateCurrency(currency);
    }

    public static Price of(BigDecimal amount) {
        return new Price(amount, MAD_CURRENCY);
    }

    public static Price of(BigDecimal amount, Currency currency) {
        return new Price(amount, currency);
    }

    public static Price zero() {
        return new Price(BigDecimal.ZERO, MAD_CURRENCY);
    }

    private BigDecimal validateAmount(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Price amount cannot be null");
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price amount cannot be negative");
        }
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    private Currency validateCurrency(Currency currency) {
        if (currency == null) {
            throw new IllegalArgumentException("Currency cannot be null");
        }
        if (!currency.getCurrencyCode().equals(MAD_CURRENCY_CODE)) {
            throw new IllegalArgumentException("Only MAD currency is supported");
        }
        return currency;
    }

    public Price add(Price other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot add prices with different currencies");
        }
        return new Price(this.amount.add(other.amount), this.currency);
    }

    public Price subtract(Price other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot subtract prices with different currencies");
        }
        BigDecimal result = this.amount.subtract(other.amount);
        if (result.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Result cannot be negative");
        }
        return new Price(result, this.currency);
    }

    public Price multiply(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        return new Price(this.amount.multiply(BigDecimal.valueOf(quantity)), this.currency);
    }

    public Price applyDiscount(BigDecimal discountPercentage) {
        if (discountPercentage == null) {
            throw new IllegalArgumentException("Discount percentage cannot be null");
        }
        if (discountPercentage.compareTo(BigDecimal.ZERO) < 0 || 
            discountPercentage.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("Discount percentage must be between 0 and 100");
        }
        BigDecimal discount = this.amount.multiply(discountPercentage)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return new Price(this.amount.subtract(discount), this.currency);
    }

    public boolean isGreaterThan(Price other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot compare prices with different currencies");
        }
        return this.amount.compareTo(other.amount) > 0;
    }

    public boolean isLessThan(Price other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot compare prices with different currencies");
        }
        return this.amount.compareTo(other.amount) < 0;
    }

    public boolean isZero() {
        return this.amount.compareTo(BigDecimal.ZERO) == 0;
    }

    @Override
    public String toString() {
        return String.format("%s MAD", amount.toPlainString());
    }
}
