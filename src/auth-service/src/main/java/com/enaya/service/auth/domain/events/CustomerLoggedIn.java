package com.enaya.service.auth.domain.events;

import lombok.Value;

import java.time.LocalDateTime;
import java.util.UUID;


public record CustomerLoggedIn(UUID authenticationId, UUID clientId, String username, LocalDateTime timestamp) {
    public static CustomerLoggedIn of(UUID authenticationId, UUID clientId, String username, LocalDateTime timestamp) {
        return new CustomerLoggedIn(authenticationId, clientId, username, timestamp);
    }
}
