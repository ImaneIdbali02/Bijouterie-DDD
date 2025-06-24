package com.enaya.service.auth.infrastructure.messaging;

import com.enaya.service.auth.domain.events.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publish(PasswordResetRequested event) {
        log.info("Publishing password reset request event for email: {}", event.email());
        kafkaTemplate.send("auth.password-reset-requested", event.email(), event);
    }

    public void publish(PasswordChanged event) {
        log.info("Publishing password changed event for user: {}", event.authenticationId());
        kafkaTemplate.send("auth.password-changed", event.authenticationId().toString(), event);
    }

    public void publish(CustomerRegistered event) {
        log.info("Publishing customer registered event for user: {}", event.authenticationId());
        kafkaTemplate.send("auth.customer-registered", event.authenticationId().toString(), event);
    }

    public void publish(CustomerLoggedIn event) {
        log.info("Publishing customer logged in event for user: {}", event.authenticationId());
        kafkaTemplate.send("auth.customer-logged-in", event.authenticationId().toString(), event);
    }

    public void publish(CustomerLoggedOut event) {
        log.info("Publishing customer logged out event for user: {}", event.authenticationId());
        kafkaTemplate.send("auth.customer-logged-out", event.authenticationId().toString(), event);
    }

    public void publish(LoginAttemptFailed event) {
        log.info("Publishing login attempt failed event for username: {}", event.usernameOrEmail());
        kafkaTemplate.send("auth.login-attempt-failed", event.usernameOrEmail(), event);
    }
}