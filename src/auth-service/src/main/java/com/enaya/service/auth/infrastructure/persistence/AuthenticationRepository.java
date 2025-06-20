package com.enaya.service.auth.infrastructure.persistence;

import com.enaya.service.auth.domain.aggregates.Authentication;

import java.util.UUID;

public interface AuthenticationRepository {

    Authentication findById(UUID id);

    Authentication findByUsername(String username);

    Authentication findByEmail(String email);

    Authentication findByProviderAndProviderId(Authentication.AuthProvider provider, String providerId);

    Authentication findByPasswordResetToken(String token);

    Authentication save(Authentication authentication);

    void delete(Authentication authentication);
}
