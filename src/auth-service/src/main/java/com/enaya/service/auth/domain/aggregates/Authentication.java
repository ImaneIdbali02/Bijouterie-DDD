package com.enaya.service.auth.domain.aggregates;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Authentification {
    private UUID id;
    private UUID clientId;
    private String username;
    private String email;
    private String hashMotDePasse;
    private boolean verified;
    private String provider; // "LOCAL", "GOOGLE", "FACEBOOK"
    private String providerUserId;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
    private boolean active;
    private boolean isAdmin;


    public static Authentification createLocalUser(UUID clientId, String username,
                                                 String email, String hashedPassword) {
        return Authentification.builder()
                .id(UUID.randomUUID())
                .clientId(clientId)
                .username(username)
                .email(email)
                .hashMotDePasse(hashedPassword)
                .verified(false)
                .provider("LOCAL")
                .createdAt(LocalDateTime.now())
                .active(true)
                .isAdmin(false)
                .build();
    }
    /**
     * Factory method to create a new social authentication
     */
    public static com.jewelryshop.auth.domain.aggregates.Authentification createSocialUser(UUID clientId, String email,
                                                                                           String provider, String providerUserId) {
        return com.jewelryshop.auth.domain.aggregates.Authentification.builder()
                .id(UUID.randomUUID())
                .clientId(clientId)
                .email(email)
                .provider(provider)
                .providerUserId(providerUserId)
                .verified(true) // Social logins are considered pre-verified
                .createdAt(LocalDateTime.now())
                .active(true)
                .isAdmin(false)
                .build();
    }

    /**
     * Updates the last login timestamp
     */
    public void recordLogin() {
        this.lastLogin = LocalDateTime.now();
    }

    /**
     * Verifies the user's account
     */
    public void verify() {
        this.verified = true;
    }

    /**
     * Deactivates the user's account
     */
    public void deactivate() {
        this.active = false;
    }

    /**
     * Reactivates the user's account
     */
    public void activate() {
        this.active = true;
    }

    /**
     * Updates the password hash
     */
    public void updatePassword(String newHashedPassword) {
        this.hashMotDePasse = newHashedPassword;
    }
}