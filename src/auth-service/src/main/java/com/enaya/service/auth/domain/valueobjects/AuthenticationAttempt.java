package com.enaya.service.auth.domain.valueobjects;

import lombok.Value;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;


@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AuthenticationAttempt {
    LocalDateTime date;
    String ip;
    boolean success;
    String username; // Could be email or username that was attempted
    String failureReason; // if success is false


    public static AuthenticationAttempt successful(String ip, String username) {
        return new AuthenticationAttempt(
                LocalDateTime.now(),
                ip,
                true,
                username,
                null
        );
    }


    public static AuthenticationAttempt failed(String ip, String username, String reason) {
        return new AuthenticationAttempt(
                LocalDateTime.now(),
                ip,
                false,
                username,
                reason
        );
    }


    public String toLogString() {
        if (success) {
            return String.format("[%s] Successful login for user '%s' from IP %s",
                    date, username, ip);
        } else {
            return String.format("[%s] Failed login attempt for user '%s' from IP %s: %s",
                    date, username, ip, failureReason);
        }
    }

    public static AuthenticationAttempt of(LocalDateTime date, String ip, boolean success, String username, String failureReason) {
        return new AuthenticationAttempt(date, ip, success, username, failureReason);
    }
}