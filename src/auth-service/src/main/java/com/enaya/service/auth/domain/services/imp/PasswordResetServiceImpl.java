package com.enaya.service.auth.domain.services.imp;

import com.enaya.service.auth.domain.aggregates.Authentication;
import com.enaya.service.auth.domain.services.interfaces.PasswordResetService;
import com.enaya.service.auth.exception.AuthenticationException;
import com.enaya.service.auth.infrastructure.persistence.AuthenticationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetService {

    private final AuthenticationRepository authenticationRepository;
    
    // Pattern pour la validation du mot de passe
    private static final Pattern PASSWORD_PATTERN = 
        Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$");

    @Override
    public String generateResetToken(Authentication authentication) {
        String token = UUID.randomUUID().toString();
        authentication.setPasswordResetToken(token);
        authentication.setPasswordResetTokenExpiry(LocalDateTime.now().plusHours(24));
        return token;
    }

    @Override
    public void validateResetToken(Authentication authentication) throws AuthenticationException {
        if (authentication.getPasswordResetToken() == null) {
            throw new AuthenticationException("No reset token found");
        }

        if (authentication.getPasswordResetTokenExpiry() == null) {
            throw new AuthenticationException("Reset token has expired");
        }

        if (LocalDateTime.now().isAfter(authentication.getPasswordResetTokenExpiry())) {
            throw new AuthenticationException("Reset token has expired");
        }
    }

    @Override
    public boolean isPasswordValid(String password) {
        return password != null && 
               password.length() >= 8 && 
               PASSWORD_PATTERN.matcher(password).matches();
    }

    @Override
    public void clearResetToken(Authentication authentication) {
        authentication.setPasswordResetToken(null);
        authentication.setPasswordResetTokenExpiry(null);
    }
}