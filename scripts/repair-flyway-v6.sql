-- Script pour réparer le checksum Flyway pour la migration V6
-- À exécuter manuellement dans la base de données PostgreSQL

-- 1. Vérifier l'état actuel des migrations
SELECT version, description, checksum, installed_on, success 
FROM flyway_schema_history 
ORDER BY version;

-- 2. Réparer le checksum pour la migration V6
-- Le nouveau checksum calculé par Flyway est: 20439592
UPDATE flyway_schema_history 
SET checksum = 20439592 
WHERE version = '6' AND description = 'add product rating columns';

-- 3. Vérifier après réparation
SELECT version, description, checksum, installed_on, success 
FROM flyway_schema_history 
WHERE version = '6';

-- 4. Si la migration V6 n'existe pas, l'ajouter manuellement
INSERT INTO flyway_schema_history (
    version, 
    description, 
    type, 
    script, 
    checksum, 
    installed_by, 
    installed_on, 
    execution_time, 
    success
) 
SELECT 
    '6',
    'add product rating columns',
    'SQL',
    'V6__add_product_rating_columns.sql',
    20439592,
    current_user,
    CURRENT_TIMESTAMP,
    0,
    true
WHERE NOT EXISTS (
    SELECT 1 FROM flyway_schema_history WHERE version = '6'
); 