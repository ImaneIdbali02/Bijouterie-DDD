package com.enaya.service.auth.application;

import com.enaya.service.auth.api.dto.*;
import com.enaya.service.auth.domain.aggregates.Authentication;
import com.enaya.service.auth.domain.events.*;
import com.enaya.service.auth.domain.services.interfaces.CredentialValidationService;
import com.enaya.service.auth.domain.services.interfaces.PasswordHashingService;
import com.enaya.service.auth.domain.services.interfaces.PasswordResetService;
import com.enaya.service.auth.domain.services.interfaces.TokenManagementService;
import com.enaya.service.auth.domain.valueobjects.OAuth2UserInfo;
import com.enaya.service.auth.exception.AuthenticationException;
import com.enaya.service.auth.infrastructure.messaging.KafkaEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final CredentialValidationService credentialValidationService;
    private final PasswordHashingService passwordHashingService;
    private final TokenManagementService tokenManagementService;
    private final PasswordResetService passwordResetService;
    private final KafkaEventPublisher eventPublisher;
    private final PasswordEncoder passwordEncoder;

    public AuthResponse register(RegisterRequest request) throws AuthenticationException {
        if (credentialValidationService.usernameExists(request.getUsername())) {
            throw new AuthenticationException("Username already taken");
        }
        if (credentialValidationService.emailExists(request.getEmail())) {
            throw new AuthenticationException("Email already registered");
        }

        if (!credentialValidationService.isValidPassword(request.getPassword())) {
            throw new AuthenticationException("Password does not meet security requirements");
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new AuthenticationException("Passwords do not match");
        }

        String passwordHash = passwordHashingService.hashPassword(request.getPassword());

        Authentication authentication = Authentication.builder()
                .id(UUID.randomUUID())
                .clientId(UUID.randomUUID())
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordHash)
                .provider(Authentication.AuthProvider.LOCAL)
                .enabled(true)
                .locked(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .version(0L)
                .role("ROLE_USER")
                .build();

        authentication = credentialValidationService.saveAuthentication(authentication);

        String accessToken = tokenManagementService.generateAccessToken(authentication);
        String refreshToken = tokenManagementService.generateRefreshToken(authentication);

        eventPublisher.publish(CustomerRegistered.of(
                authentication.getId(),
                authentication.getClientId(),
                authentication.getUsername(),
                authentication.getEmail(),
                LocalDateTime.now()
        ));

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(tokenManagementService.getAccessTokenExpiryTime())
                .clientId(authentication.getClientId().toString())
                .email(authentication.getEmail())
                .username(authentication.getUsername())
                .build();
    }

    public AuthResponse login(@Valid LoginRequest request) throws AuthenticationException {
        Authentication authentication;
        try {
            authentication = credentialValidationService.validateCredentials(
                    request.getUsernameOrEmail(),
                    request.getPassword()
            );
        } catch (AuthenticationException e) {
            eventPublisher.publish(LoginAttemptFailed.of(
                    null,
                    request.getUsernameOrEmail(),
                    "Invalid credentials",
                    LocalDateTime.now()
            ));

            throw e;
        }

        String accessToken = tokenManagementService.generateAccessToken(authentication);
        String refreshToken = tokenManagementService.generateRefreshToken(authentication);

        eventPublisher.publish(CustomerLoggedIn.of(
                authentication.getId(),
                authentication.getClientId(),
                authentication.getUsername(),
                LocalDateTime.now()
        ));

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(tokenManagementService.getAccessTokenExpiryTime())
                .clientId(authentication.getClientId().toString())
                .email(authentication.getEmail())
                .username(authentication.getUsername())
                .build();
    }

    public AuthResponse oauth2Login(@Valid OAuth2AuthRequest request) throws AuthenticationException {
        Authentication authentication;
        Authentication.AuthProvider provider;

        try {
            provider = Authentication.AuthProvider.valueOf(request.getProvider().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new AuthenticationException("Unsupported authentication provider");
        }

        OAuth2UserInfo userInfo = validateOAuth2Token(request.getToken(), provider);

        authentication = credentialValidationService.findByProviderAndProviderId(
                provider,
                userInfo.getId()
        );

        if (authentication == null) {
            authentication = Authentication.builder()
                    .id(UUID.randomUUID())
                    .clientId(UUID.randomUUID())
                    .username(userInfo.getName())
                    .email(userInfo.getEmail())
                    .provider(provider)
                    .providerUserId(userInfo.getId())
                    .enabled(true)
                    .locked(false)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .role("ROLE_USER")
                    .build();

            authentication = credentialValidationService.saveAuthentication(authentication);

            eventPublisher.publish(CustomerRegistered.of(
                    authentication.getId(),
                    authentication.getClientId(),
                    authentication.getUsername(),
                    authentication.getEmail(),
                    LocalDateTime.now()
            ));
        }

        String accessToken = tokenManagementService.generateAccessToken(authentication);
        String refreshToken = tokenManagementService.generateRefreshToken(authentication);

        eventPublisher.publish(CustomerLoggedIn.of(
                authentication.getId(),
                authentication.getClientId(),
                authentication.getUsername(),
                LocalDateTime.now()
        ));

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(tokenManagementService.getAccessTokenExpiryTime())
                .clientId(authentication.getClientId().toString())
                .email(authentication.getEmail())
                .username(authentication.getUsername())
                .build();
    }

    private OAuth2UserInfo validateOAuth2Token(String token, Authentication.AuthProvider provider) {
        return OAuth2UserInfo.builder()
                .id("mock_provider_id")
                .name("John Doe")
                .email("john.doe@example.com")
                .build();
    }

    public void logout(String token) {
        String accessToken = token.startsWith("Bearer ") ? token.substring(7) : token;
        UUID authId = tokenManagementService.invalidateAccessToken(accessToken);

        if (authId != null) {
            eventPublisher.publish(CustomerLoggedOut.of(
                    authId,
                    LocalDateTime.now()
            ));
        }
    }

    public AuthResponse refreshToken(@Valid RefreshTokenRequest request) throws AuthenticationException {
        Authentication authentication = tokenManagementService.validateRefreshToken(request.getRefreshToken());
        String accessToken = tokenManagementService.generateAccessToken(authentication);
        String refreshToken = tokenManagementService.generateRefreshToken(authentication);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(tokenManagementService.getAccessTokenExpiryTime())
                .clientId(authentication.getClientId().toString())
                .email(authentication.getEmail())
                .username(authentication.getUsername())
                .build();
    }

    @Transactional
    public void requestPasswordReset(PasswordResetRequest request) throws AuthenticationException {
        Authentication authentication = credentialValidationService.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthenticationException("Email not found"));

        String resetToken = passwordResetService.generateResetToken(authentication);
        credentialValidationService.saveAuthentication(authentication);

        // Publier l'événement pour le service de notification
        eventPublisher.publish(PasswordResetRequested.of(
                request.getEmail(),
                resetToken,
                LocalDateTime.now()
        ));
    }

    @Transactional
    public void resetPassword(PasswordResetConfirmation request) throws AuthenticationException {
        Authentication authentication = credentialValidationService.findByPasswordResetToken(request.getToken())
                .orElseThrow(() -> new AuthenticationException("Invalid or expired reset token"));

        passwordResetService.validateResetToken(authentication);

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new AuthenticationException("Passwords do not match");
        }

        if (!passwordResetService.isPasswordValid(request.getPassword())) {
            throw new AuthenticationException("Password does not meet security requirements");
        }

        String passwordHash = passwordHashingService.hashPassword(request.getPassword());
        authentication.setPasswordHash(passwordHash);
        passwordResetService.clearResetToken(authentication);

        credentialValidationService.saveAuthentication(authentication);
        tokenManagementService.invalidateAllTokens(authentication.getId());

        eventPublisher.publish(PasswordChanged.of(
                authentication.getId(),
                authentication.getClientId(),
                LocalDateTime.now()
        ));
    }

    public boolean verifyToken(String token) {
        String accessToken = token.startsWith("Bearer ") ? token.substring(7) : token;
        return tokenManagementService.verifyAccessToken(accessToken);
    }
}