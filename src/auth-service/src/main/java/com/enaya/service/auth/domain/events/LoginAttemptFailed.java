package com.enaya.service.auth.domain.events;

import lombok.Value;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @param authenticationId nullable si login échoue avant identification
 */


public record LoginAttemptFailed(UUID authenticationId, String usernameOrEmail, String reason,
                                 LocalDateTime timestamp) {
    public static LoginAttemptFailed of(UUID authenticationId, String usernameOrEmail, String reason, LocalDateTime timestamp) {
        return new LoginAttemptFailed(authenticationId, usernameOrEmail, reason, timestamp);
    }
}
