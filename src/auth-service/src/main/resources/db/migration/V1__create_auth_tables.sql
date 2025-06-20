-- Script de migration Flyway V1
-- Table pour les authentifications (JPA/Hibernate)
CREATE TABLE authentications (
                                 id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                 client_id UUID NOT NULL DEFAULT gen_random_uuid(),
                                 username VARCHAR(50) UNIQUE NOT NULL,
                                 email VARCHAR(255) UNIQUE NOT NULL,
                                 password_hash VARCHAR(255),
                                 provider VARCHAR(20) NOT NULL DEFAULT 'LOCAL',
                                 provider_user_id VARCHAR(255),
                                 enabled BOOLEAN NOT NULL DEFAULT TRUE,
                                 locked BOOLEAN NOT NULL DEFAULT FALSE,
                                 created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                                 updated_at TIMESTAMP NOT NULL DEFAULT NOW(),

                                 CONSTRAINT uk_auth_provider_user UNIQUE (provider, provider_user_id)
);

-- Index pour les recherches fréquentes
CREATE INDEX idx_auth_username_lower ON authentications (LOWER(username));
CREATE INDEX idx_auth_email_lower ON authentications (LOWER(email));
CREATE INDEX idx_auth_provider_user ON authentications (provider, provider_user_id);

-- Table pour les sessions (JDBC Template)
CREATE TABLE session_tokens (
                                token UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                expires_at TIMESTAMP NOT NULL,
                                authentication_id UUID NOT NULL REFERENCES authentications(id) ON DELETE CASCADE,
                                ip_address INET,
                                user_agent TEXT,
                                created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Index pour le nettoyage des sessions expirées
CREATE INDEX idx_session_expires ON session_tokens (expires_at);
CREATE INDEX idx_session_user ON session_tokens (authentication_id);

-- Table pour les tentatives d'authentification (JDBC Template)
CREATE TABLE authentication_attempts (
                                         id BIGSERIAL PRIMARY KEY,
                                         date TIMESTAMP NOT NULL DEFAULT NOW(),
                                         ip_address INET NOT NULL,
                                         success BOOLEAN NOT NULL,
                                         username VARCHAR(255) NOT NULL,
                                         failure_reason VARCHAR(500),
                                         created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Index pour les recherches de sécurité
CREATE INDEX idx_auth_attempts_ip_date ON authentication_attempts (ip_address, date);
CREATE INDEX idx_auth_attempts_username_date ON authentication_attempts (username, date);
CREATE INDEX idx_auth_attempts_success_date ON authentication_attempts (success, date);

-- Fonction pour nettoyer automatiquement les anciennes tentatives
CREATE OR REPLACE FUNCTION cleanup_old_auth_attempts() RETURNS void AS $$
BEGIN
    DELETE FROM authentication_attempts WHERE created_at < NOW() - INTERVAL '30 days';
END;
$$ LANGUAGE plpgsql;