package com.enaya.service.auth.domain.services.interfaces;

import com.enaya.service.auth.domain.aggregates.Authentication;
import com.enaya.service.auth.exception.AuthenticationException;

public interface PasswordResetService {
    /**
     * Génère un token de réinitialisation pour un utilisateur
     */
    String generateResetToken(Authentication authentication);

    /**
     * Valide un token de réinitialisation
     */
    void validateResetToken(Authentication authentication) throws AuthenticationException;

    /**
     * Vérifie si un mot de passe est valide selon les règles de sécurité
     */
    boolean isPasswordValid(String password);

    /**
     * Efface le token de réinitialisation après utilisation
     */
    void clearResetToken(Authentication authentication);
}