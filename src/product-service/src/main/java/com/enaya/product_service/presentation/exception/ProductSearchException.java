package com.enaya.product_service.presentation.exception;

/**
 * Custom exception for product search and repository operations.
 * Indicates issues that occur during product search or persistence operations.
 */
public class ProductSearchException extends RuntimeException {

    /**
     * Constructs a new ProductSearchException with the specified detail message.
     * @param message the detail message
     */
    public ProductSearchException(String message) {
        super(message);
    }

    /**
     * Constructs a new ProductSearchException with the specified detail message and cause.
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public ProductSearchException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new ProductSearchException with the specified cause.
     * @param cause the cause of the exception
     */
    public ProductSearchException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new ProductSearchException with the specified detail message,
     * cause, suppression enabled or disabled, and writable stack trace enabled or disabled.
     * @param message the detail message
     * @param cause the cause of the exception
     * @param enableSuppression whether suppression is enabled or disabled
     * @param writableStackTrace whether the stack trace should be writable
     */
    protected ProductSearchException(String message, Throwable cause,
                                     boolean enableSuppression,
                                     boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}