package com.enaya.product_service.application.exception;

public class ProductIndexingException extends RuntimeException {
    
    public ProductIndexingException(String message) {
        super(message);
    }
    
    public ProductIndexingException(String message, Throwable cause) {
        super(message, cause);
    }
} 