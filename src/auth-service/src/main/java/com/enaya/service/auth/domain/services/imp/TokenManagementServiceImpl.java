package com.enaya.service.auth.domain.services.imp;

import com.enaya.service.auth.config.JwtConfig;
import com.enaya.service.auth.domain.aggregates.Authentication;
import com.enaya.service.auth.domain.services.interfaces.TokenManagementService;

import com.enaya.service.auth.exception.AuthenticationException;
import com.enaya.service.auth.infrastructure.persistence.AuthenticationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
@RequiredArgsConstructor
public class TokenManagementServiceImpl implements TokenManagementService {

    private final JwtConfig jwtConfig;
    private final AuthenticationRepository authenticationRepository;

    // In-memory token blacklist (should be replaced with Redis in production)
    private final Set<String> tokenBlacklist = new HashSet<>();
    private final Map<UUID, Set<String>> userTokens = new HashMap<>();

    @Override
    public String generateAccessToken(Authentication authentication) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", authentication.getRole()); // Dynamically assign role

        // Store token reference for the user
        String token = jwtConfig.generateToken(claims, jwtConfig.getAccessTokenExpirationMs(), authentication.getId().toString());
        userTokens.computeIfAbsent(authentication.getId(), k -> new HashSet<>()).add(token);

        return token;
    }

    @Override
    public String generateRefreshToken(Authentication authentication) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");

        // Store token reference
        String token = jwtConfig.generateToken(claims, jwtConfig.getRefreshTokenExpirationMs(), authentication.getId().toString());
        userTokens.computeIfAbsent(authentication.getId(), k -> new HashSet<>()).add(token);

        return token;
    }

    @Override
    public Authentication validateRefreshToken(String refreshToken) throws AuthenticationException {
        if (!jwtConfig.validateToken(refreshToken)) {
            throw new AuthenticationException("Invalid refresh token");
        }

        if (tokenBlacklist.contains(refreshToken)) {
            throw new AuthenticationException("Refresh token has been invalidated");
        }

        // Get user ID from token
        String userIdStr = jwtConfig.getSubject(refreshToken);
        UUID userId = UUID.fromString(userIdStr);

        // Check token type
        String tokenType = jwtConfig.getClaim(refreshToken, claims ->
                claims.get("type", String.class));

        if (!"refresh".equals(tokenType)) {
            throw new AuthenticationException("Invalid token type");
        }

        // Get authentication
        Authentication authentication = authenticationRepository.findById(userId);
        if (authentication == null) {
            throw new AuthenticationException("User not found");
        }

        // Check if account is locked or disabled
        if (!authentication.isEnabled()) {
            throw new AuthenticationException("Account is disabled");
        }

        if (authentication.isLocked()) {
            throw new AuthenticationException("Account is locked");
        }

        return authentication;
    }

    @Override
    public boolean verifyAccessToken(String accessToken) {
        if (!jwtConfig.validateToken(accessToken)) {
            return false;
        }

        return !tokenBlacklist.contains(accessToken);
    }

    @Override
    public UUID invalidateAccessToken(String accessToken) {
        if (!jwtConfig.validateToken(accessToken)) {
            return null;
        }

        // Add to blacklist
        tokenBlacklist.add(accessToken);

        // Get user ID from token
        String userIdStr = jwtConfig.getSubject(accessToken);
        return UUID.fromString(userIdStr);
    }

    @Override
    public void invalidateAllTokens(UUID authenticationId) {
        Set<String> tokens = userTokens.get(authenticationId);
        if (tokens != null) {
            tokenBlacklist.addAll(tokens);
            tokens.clear();
        }
    }

    @Override
    public long getAccessTokenExpiryTime() {
        return jwtConfig.getAccessTokenExpirationMs() / 1000; // Convert to seconds
    }
}