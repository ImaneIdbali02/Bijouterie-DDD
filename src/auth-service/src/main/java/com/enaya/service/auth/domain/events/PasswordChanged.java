package com.enaya.service.auth.domain.events;

import lombok.Value;
import java.time.LocalDateTime;
import java.util.UUID;


public record PasswordChanged(UUID authenticationId, UUID clientId, LocalDateTime timestamp) {
    public static PasswordChanged of(UUID authenticationId, UUID clientId, LocalDateTime timestamp) {
        return new PasswordChanged(authenticationId, clientId, timestamp);
    }
}
