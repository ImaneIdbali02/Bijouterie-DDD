-- Script de migration Flyway V5
-- Mettre à jour le mot de passe de l'utilisateur admin

UPDATE authentications
SET password_hash = '$2a$12$C.wS4HtXRm9CnnMabLDPd.w5zBdyx7PKij.QEmt8BS3GbxL075t0q',
    updated_at = NOW()
WHERE email = 'enaya@gmail.com'; 