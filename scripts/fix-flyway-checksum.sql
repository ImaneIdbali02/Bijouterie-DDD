-- Script pour réparer le checksum Flyway pour la migration V6
-- À exécuter manuellement dans la base de données si nécessaire

-- Vérifier l'état actuel des migrations
SELECT version, description, checksum, installed_on, success 
FROM flyway_schema_history 
ORDER BY version;

-- Réparer le checksum pour la migration V6
-- Le nouveau checksum est calculé par Flyway automatiquement
UPDATE flyway_schema_history 
SET checksum = 20439592 
WHERE version = '6' AND description = 'add product rating columns';

-- Vérifier après réparation
SELECT version, description, checksum, installed_on, success 
FROM flyway_schema_history 
WHERE version = '6'; 