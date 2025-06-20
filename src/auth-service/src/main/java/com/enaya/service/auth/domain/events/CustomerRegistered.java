package com.enaya.service.auth.domain.events;

import lombok.Value;
import java.time.LocalDateTime;
import java.util.UUID;


public record CustomerRegistered(UUID authenticationId, UUID clientId, String username, String email,
                                 LocalDateTime timestamp) {
    public static CustomerRegistered of(UUID authenticationId, UUID clientId, String username, String email, LocalDateTime timestamp) {
        return new CustomerRegistered(authenticationId, clientId, username, email, timestamp);
    }
}
