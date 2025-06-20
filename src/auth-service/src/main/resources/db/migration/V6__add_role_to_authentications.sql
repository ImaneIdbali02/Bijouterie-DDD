-- Script de migration Flyway V6
-- Mettre à jour le rôle de l'utilisateur admin
UPDATE authentications
SET role = 'ROLE_ADMIN'
WHERE email = 'enaya@gmail.com'; 