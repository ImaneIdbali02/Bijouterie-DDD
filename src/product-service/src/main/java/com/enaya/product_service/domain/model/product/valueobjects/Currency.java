package com.enaya.product_service.domain.model.product.valueobjects;

import lombok.Value;

@Value
public class Currency {
    public static final Currency MAD = new Currency("MAD", "Dirham marocain", "د.م.");
    
    private final String code;
    
    private final String name;
    
    private final String symbol;

    private Currency(String code, String name, String symbol) {
        this.code = code;
        this.name = name;
        this.symbol = symbol;
    }

    public static Currency of(String code) {
        if (MAD.getCode().equals(code)) {
            return MAD;
        }
        throw new IllegalArgumentException("Currency code '" + code + "' is not supported. Only MAD currency is supported.");
    }

    @Override
    public String toString() {
        return code;
    }
} 