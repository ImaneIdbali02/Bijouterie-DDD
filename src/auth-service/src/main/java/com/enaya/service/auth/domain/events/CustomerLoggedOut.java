package com.enaya.service.auth.domain.events;

import lombok.Value;
import java.time.LocalDateTime;
import java.util.UUID;


public record CustomerLoggedOut(UUID authenticationId, LocalDateTime timestamp) {
    public static CustomerLoggedOut of(UUID authenticationId, LocalDateTime timestamp) {
        return new CustomerLoggedOut(authenticationId, timestamp);
    }
}
