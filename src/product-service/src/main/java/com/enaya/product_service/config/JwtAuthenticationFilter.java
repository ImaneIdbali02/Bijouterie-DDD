package com.enaya.product_service.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtConfig jwtConfig;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        log.info("JwtAuthenticationFilter: Entering doFilterInternal for request URI: {}", request.getRequestURI());

        try {
            String jwt = getJwtFromRequest(request);
            log.debug("JWT token from request: {}", jwt != null ? "present" : "not present");

            if (jwt != null) {
                log.debug("JWT token preview: {}...", jwt.substring(0, Math.min(jwt.length(), 30)));
            }

            if (StringUtils.hasText(jwt)) {
                log.debug("Validating JWT token");
                if (jwtConfig.validateToken(jwt)) {
                    log.debug("JWT token is valid, extracting claims");
                    // Get user ID from JWT
                    String userIdStr = jwtConfig.getSubject(jwt);
                    log.debug("User ID from JWT: {}", userIdStr);
                    UUID userId = UUID.fromString(userIdStr);

                    // Get user role from JWT claims
                    String role = jwtConfig.getClaim(jwt, claims ->
                            claims.get("role", String.class) != null ?
                                    claims.get("role", String.class) : "ROLE_USER");
                    log.debug("User role from JWT: {}", role);

                    // Create authentication object
                    Authentication authentication = new UsernamePasswordAuthenticationToken(
                            userId,
                            null,
                            Collections.singletonList(new SimpleGrantedAuthority(role))
                    );

                    // Set authentication in Spring Security context
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.info("Authentication set in SecurityContext for user: {} with role: {}", userId, role);
                } else {
                    log.warn("JWT token validation failed");
                }
            } else {
                log.debug("No JWT token found in request");
            }
        } catch (Exception ex) {
            log.error("Error processing JWT token: {}", ex.getMessage(), ex);
            // Clear security context on error
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
        log.debug("JwtAuthenticationFilter: Completed processing for request URI: {}", request.getRequestURI());
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}