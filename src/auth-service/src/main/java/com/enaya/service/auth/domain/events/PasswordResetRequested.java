package com.enaya.service.auth.domain.events;

import lombok.Value;
import java.time.LocalDateTime;


public record PasswordResetRequested(String email, String resetToken, LocalDateTime timestamp) {
    public static PasswordResetRequested of(String email, String resetToken, LocalDateTime timestamp) {
        return new PasswordResetRequested(email, resetToken, timestamp);
    }
}
