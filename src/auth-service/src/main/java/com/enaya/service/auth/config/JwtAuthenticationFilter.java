package com.enaya.service.auth.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
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
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtConfig jwtConfig;
    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Extraire le token du header
        final String jwt = getJwtFromRequest(request);

        // Si pas de token, on continue la chaîne de filtres.
        // Les endpoints publics passeront, les privés seront bloqués plus tard.
        if (!StringUtils.hasText(jwt)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 2. Valider le token
            if (jwtConfig.validateToken(jwt)) {
                // 3. Extraire les informations et créer l'objet Authentication
                String userIdStr = jwtConfig.getSubject(jwt);
                UUID userId = UUID.fromString(userIdStr);
                String role = jwtConfig.getClaim(jwt, claims -> claims.get("role", String.class));

                if (role != null) {
                    Authentication authentication = new UsernamePasswordAuthenticationToken(
                            userId,
                            null,
                            Collections.singletonList(new SimpleGrantedAuthority(role))
                    );
                    // 4. Placer l'authentification dans le contexte de sécurité
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception e) {
            // En cas d'erreur (token expiré, invalide), on ne lève pas d'exception ici.
            // On se contente de ne pas mettre d'objet Authentication dans le contexte.
            // La chaîne de filtres continue, et c'est le filtre d'autorisation qui rejettera la requête.
            log.debug("Could not set user authentication in security context", e);
        }

        // 5. Toujours continuer la chaîne de filtres
        filterChain.doFilter(request, response);
    }


    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}