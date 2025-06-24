package com.enaya.service.auth.domain.services.interfaces;

import com.enaya.service.auth.domain.aggregates.Authentication;
import com.enaya.service.auth.exception.AuthenticationException;

import java.util.Optional;

public interface CredentialValidationService {
    /**
     * Check if username already exists
     */
    boolean usernameExists(String username);

    /**
     * Check if email already exists
     */
    boolean emailExists(String email);

    /**
     * Validate password strength
     */
    boolean isValidPassword(String password);

    /**
     * Validate user credentials
     */
    Authentication validateCredentials(String usernameOrEmail, String password) throws AuthenticationException;

    /**
     * Find authentication by provider and provider ID
     */
    Authentication findByProviderAndProviderId(Authentication.AuthProvider provider, String providerId);

    /**
     * Save authentication entity
     */
    Authentication saveAuthentication(Authentication authentication);

    /**
     * Find authentication by email
     */
    Optional<Authentication> findByEmail(String email);

    /**
     * Find authentication by password reset token
     */
    Optional<Authentication> findByPasswordResetToken(String token);
}
