package com.enaya.product_service.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

@Configuration
@EnableRetry
public class RetryConfig {
    // La configuration par défaut est suffisante pour notre cas
} 