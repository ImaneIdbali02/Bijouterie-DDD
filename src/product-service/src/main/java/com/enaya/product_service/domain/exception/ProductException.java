package com.enaya.product_service.domain.exception;

import java.util.UUID;

public class ProductException extends RuntimeException {
    
    public ProductException(String message) {
        super(message);
    }

    public ProductException(String message, Throwable cause) {
        super(message, cause);
    }

    public static class ProductNotFoundException extends ProductException {
        public ProductNotFoundException(UUID productId) {
            super(String.format("Product with id %s not found", productId));
        }
    }

    public static class ProductValidationException extends ProductException {
        public ProductValidationException(String message) {
            super(message);
        }
    }

    public static class ProductOperationException extends ProductException {
        public ProductOperationException(String message) {
            super(message);
        }
    }
} 