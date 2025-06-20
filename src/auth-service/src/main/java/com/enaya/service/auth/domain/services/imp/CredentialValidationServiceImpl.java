package com.enaya.service.auth.domain.services.imp;

import com.enaya.service.auth.domain.aggregates.Authentication;
import com.enaya.service.auth.domain.services.interfaces.CredentialValidationService;
import com.enaya.service.auth.domain.services.interfaces.PasswordHashingService;
import com.enaya.service.auth.exception.AuthenticationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.enaya.service.auth.infrastructure.persistence.AuthenticationRepository;
import java.util.regex.Pattern;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Slf4j
public class CredentialValidationServiceImpl implements CredentialValidationService {

    private final AuthenticationRepository authenticationRepository;
    private final PasswordHashingService passwordHashingService;

    // Password pattern: at least 8 chars, 1 uppercase, 1 lowercase, 1 number
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$");

    @Override
    public boolean usernameExists(String username) {
        return authenticationRepository.findByUsername(username) != null;
    }

    @Override
    public boolean emailExists(String email) {
        return authenticationRepository.findByEmail(email) != null;
    }

    @Override
    public boolean isValidPassword(String password) {
        return password != null &&
                password.length() >= 8 &&
                PASSWORD_PATTERN.matcher(password).matches();
    }

    @Override
    public Authentication validateCredentials(String usernameOrEmail, String password) throws AuthenticationException {
        // Find by username or email
        Authentication authentication = authenticationRepository.findByUsername(usernameOrEmail);
        if (authentication == null) {
            authentication = authenticationRepository.findByEmail(usernameOrEmail);
        }

        if (authentication == null) {
            throw new AuthenticationException("Invalid username or email");
        }

        // Check if account is locked or disabled
        if (!authentication.isEnabled()) {
            throw new AuthenticationException("Account is disabled");
        }

        if (authentication.isLocked()) {
            throw new AuthenticationException("Account is locked");
        }

        // For OAuth users who don't have a password
        if (authentication.getProvider() != Authentication.AuthProvider.LOCAL) {
            throw new AuthenticationException("Please use " + authentication.getProvider() + " to login");
        }

        // Verify password
        if (!passwordHashingService.verifyPassword(password, authentication.getPasswordHash())) {
            throw new AuthenticationException("Invalid password");
        }

        return authentication;
    }

    @Override
    public Authentication findByProviderAndProviderId(
            Authentication.AuthProvider provider, String providerId) {
        return authenticationRepository.findByProviderAndProviderId(provider, providerId);
    }

    @Override
    public Authentication saveAuthentication(Authentication authentication) {
        return authenticationRepository.save(authentication);
    }

    @Override
    public Optional<Authentication> findByEmail(String email) {
        Authentication auth = authenticationRepository.findByEmail(email);
        return Optional.ofNullable(auth);
    }

    @Override
    public Optional<Authentication> findByPasswordResetToken(String token) {
        Authentication auth = authenticationRepository.findByPasswordResetToken(token);
        return Optional.ofNullable(auth);
    }
}