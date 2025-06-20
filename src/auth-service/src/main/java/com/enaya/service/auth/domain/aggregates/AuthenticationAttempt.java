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
public class AuthenticationAttempt {
    private UUID id;
    private UUID authenticationId;
    private LocalDateTime timestamp;
    private String ipAddress;
    private boolean successful;
    private String failureReason;
}