package com.enaya.service.auth.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.extern.slf4j.Slf4j;
import jakarta.annotation.PostConstruct;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;
import java.util.UUID;

@Configuration
@ConfigurationProperties(prefix = "app.jwt")
@Data
@Slf4j
public class JwtConfig {
    private String secret;
    private long accessTokenExpirationMs = 3600000; // 1 hour by default
    private long refreshTokenExpirationMs = 86400000; // 24 hours by default
    private Key key;

    @PostConstruct
    public void init() {
        if (secret == null || secret.trim().isEmpty()) {
            throw new IllegalStateException("JWT secret key must not be null or empty");
        }
        log.debug("JWT secret key loaded: {}", secret);
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        key = Keys.hmacShaKeyFor(keyBytes);
    }

    public Key getSigningKey() {
        if (key == null) {
            init();
        }
        return key;
    }

    public String generateToken(Map<String, Object> claims, long expirationMs, String subject) {
        if (subject == null || subject.trim().isEmpty()) {
            throw new IllegalArgumentException("Token subject cannot be null or empty");
        }
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public String generateToken(Map<String, Object> claims, long expirationMs) {
        return generateToken(claims, expirationMs, UUID.randomUUID().toString());
    }

    public Claims getAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.error("Error parsing JWT token: {}", e.getMessage());
            throw e;
        }
    }

    public <T> T getClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String getSubject(String token) {
        String subject = getClaim(token, Claims::getSubject);
        if (subject == null || subject.trim().isEmpty()) {
            log.error("JWT token subject is null or empty");
            throw new IllegalArgumentException("Invalid token: missing subject");
        }
        return subject;
    }

    public Date getExpiration(String token) {
        return getClaim(token, Claims::getExpiration);
    }

    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpiration(token);
            return expiration.before(new Date());
        } catch (Exception e) {
            log.error("Error checking token expiration: {}", e.getMessage());
            return true;
        }
    }

    public boolean validateToken(String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                log.error("Token is null or empty");
                return false;
            }

            log.debug("Validating JWT token: {}", token);
            Claims claims = getAllClaims(token);
            String subject = claims.getSubject();

            if (subject == null || subject.trim().isEmpty()) {
                log.error("Token subject is null or empty");
                return false;
            }

            // Validate UUID format
            try {
                UUID.fromString(subject);
            } catch (IllegalArgumentException e) {
                log.error("Invalid UUID format in token subject: {}", subject);
                return false;
            }

            boolean isValid = !isTokenExpired(token);
            log.debug("Token validation result: {}", isValid);
            return isValid;
        } catch (Exception e) {
            log.error("Error validating token: {}", e.getMessage(), e);
            return false;
        }
    }
}