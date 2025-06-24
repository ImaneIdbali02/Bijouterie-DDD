-- Script de nettoyage complet de la base de données pour résoudre les problèmes de checksum
-- ATTENTION: Ce script supprime toutes les données existantes

-- Supprimer toutes les tables existantes
DROP TABLE IF EXISTS flyway_schema_history CASCADE;
DROP TABLE IF EXISTS product_variant_attributes CASCADE;
DROP TABLE IF EXISTS product_variant_images CASCADE;
DROP TABLE IF EXISTS product_variants CASCADE;
DROP TABLE IF EXISTS product_attributes CASCADE;
DROP TABLE IF EXISTS product_images CASCADE;
DROP TABLE IF EXISTS product_collections CASCADE;
DROP TABLE IF EXISTS products CASCADE;
DROP TABLE IF EXISTS categories CASCADE;
DROP TABLE IF EXISTS collections CASCADE;

-- Supprimer les séquences si elles existent
DROP SEQUENCE IF EXISTS hibernate_sequence;

-- Recréer la table flyway_schema_history
CREATE TABLE flyway_schema_history (
    installed_rank INTEGER NOT NULL,
    version VARCHAR(50),
    description VARCHAR(200) NOT NULL,
    type VARCHAR(20) NOT NULL,
    script VARCHAR(1000) NOT NULL,
    checksum INTEGER,
    installed_by VARCHAR(100) NOT NULL,
    installed_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    execution_time INTEGER NOT NULL,
    success BOOLEAN NOT NULL,
    CONSTRAINT flyway_schema_history_pk PRIMARY KEY (installed_rank)
);

-- Créer les index pour flyway
CREATE INDEX flyway_schema_history_s_idx ON flyway_schema_history (success);
CREATE INDEX flyway_schema_history_v_idx ON flyway_schema_history (version);

-- Message de confirmation
DO $$
BEGIN
    RAISE NOTICE 'Base de données nettoyée avec succès. Vous pouvez maintenant relancer les migrations Flyway.';
END $$; 