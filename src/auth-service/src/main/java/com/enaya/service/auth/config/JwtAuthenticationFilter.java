package com.enaya.service.auth.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Collections;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtConfig jwtConfig;
    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Skip JWT filter for public endpoints
        if (isPublicEndpoint(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String jwt = getJwtFromRequest(request);

            if (!StringUtils.hasText(jwt)) {
                log.debug("No JWT token found in request");
                handleAuthenticationError(response, "Missing authentication token");
                return;
            }

            log.debug("JWT token from request: present");
            log.debug("JWT token preview: {}", jwt.substring(0, Math.min(20, jwt.length())) + "...");

            if (!jwtConfig.validateToken(jwt)) {
                log.debug("JWT token validation failed");
                handleAuthenticationError(response, "Invalid or expired token");
                return;
            }

            log.debug("JWT token is valid, extracting claims");
            
            // Get user ID from JWT
            String userIdStr = jwtConfig.getSubject(jwt);
            if (userIdStr == null) {
                log.error("JWT token validation failed: missing user ID");
                handleAuthenticationError(response, "Invalid token: missing user ID");
                return;
            }

            UUID userId;
            try {
                userId = UUID.fromString(userIdStr);
                log.debug("User ID from JWT: {}", userId);
            } catch (IllegalArgumentException e) {
                log.error("Invalid UUID format in JWT subject: {}", e.getMessage());
                handleAuthenticationError(response, "Invalid token: invalid user ID format");
                return;
            }

            // Get user role from JWT claims
            String role = jwtConfig.getClaim(jwt, claims -> {
                String roleClaim = claims.get("role", String.class);
                if (roleClaim == null || roleClaim.trim().isEmpty()) {
                    throw new IllegalArgumentException("JWT role claim is missing or empty");
                }
                return roleClaim;
            });

            // Create authentication object
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userId,
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority(role))
            );

            // Set authentication in Spring Security context
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);

        } catch (Exception ex) {
            log.error("Error processing JWT token: {}", ex.getMessage());
            handleAuthenticationError(response, "Authentication failed: " + ex.getMessage());
        }
    }

    private boolean isPublicEndpoint(String requestURI) {
        return requestURI.startsWith("/api/auth/register") ||
               requestURI.startsWith("/api/auth/login") ||
               requestURI.startsWith("/api/auth/oauth2/login") ||
               requestURI.startsWith("/api/auth/refresh-token") ||
               requestURI.startsWith("/api/auth/password/reset-request") ||
               requestURI.startsWith("/api/auth/password/reset");
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private void handleAuthenticationError(HttpServletResponse response, String message) throws IOException {
        SecurityContextHolder.clearContext();
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"" + message + "\"}");
    }
}