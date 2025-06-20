package com.enaya.product_service.infrastructure.messaging;

public interface EventPublisher {
    void publish(String topic, Object event);
} 