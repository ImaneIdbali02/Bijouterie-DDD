package com.enaya.service.auth.domain.aggregates;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Authentication aggregate root
 * Represents user authentication data in the system
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Authentication {
    private UUID id;
    private UUID clientId;
    private String username;
    private String email;
    private String passwordHash;
    private AuthProvider provider;
    private String providerUserId;
    private boolean enabled;
    private boolean locked;
    private String passwordResetToken;
    private LocalDateTime passwordResetTokenExpiry;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long version;
    private String role;

    public enum AuthProvider {
        LOCAL,
        GOOGLE,
        FACEBOOK,
    }

    public void lock() {
        if (!this.locked) {
            this.locked = true;
            this.updatedAt = LocalDateTime.now();
        }
    }

    public void unlock() {
        if (this.locked) {
            this.locked = false;
            this.updatedAt = LocalDateTime.now();
        }
    }
}
