package com.enaya.service.auth.domain.valueobjects;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.UUID;



@Builder
public record SessionToken(UUID token, LocalDateTime dateExpiration, UUID userId, String ipAddress, String userAgent) {
    public static SessionToken create(UUID userId, String ipAddress, String userAgent) {
        return SessionToken.builder()
                .token(UUID.randomUUID())
                .dateExpiration(LocalDateTime.now().plusHours(24))
                .userId(userId)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();
    }


    public static SessionToken create(UUID userId, String ipAddress, String userAgent, int expirationHours) {
        return SessionToken.builder()
                .token(UUID.randomUUID())
                .dateExpiration(LocalDateTime.now().plusHours(expirationHours))
                .userId(userId)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();
    }


    public boolean isExpired() {
        return LocalDateTime.now().isAfter(dateExpiration);
    }


    public SessionToken extend(int additionalHours) {
        return SessionToken.builder()
                .token(this.token)
                .dateExpiration(LocalDateTime.now().plusHours(additionalHours))
                .userId(this.userId)
                .ipAddress(this.ipAddress)
                .userAgent(this.userAgent)
                .build();
    }
}