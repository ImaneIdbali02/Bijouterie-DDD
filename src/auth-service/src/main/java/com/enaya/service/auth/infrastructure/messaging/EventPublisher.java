package com.enaya.service.auth.infrastructure.messaging;

import com.enaya.service.auth.domain.events.*;

public interface EventPublisher {
    void publish(Object event);
}