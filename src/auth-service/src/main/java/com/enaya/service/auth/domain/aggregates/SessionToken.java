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
public class SessionToken {
    private String token;
    private LocalDateTime expiresAt;
    private UUID authenticationId;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}