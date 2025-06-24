-- Script de nettoyage complet de la base de données product_db
-- Exécuter ce script avant de redémarrer l'application

-- Désactiver les contraintes de clés étrangères
SET session_replication_role = replica;

-- Supprimer toutes les tables dans l'ordre correct
DROP TABLE IF EXISTS collection_images CASCADE;
DROP TABLE IF EXISTS product_variant_images CASCADE;
DROP TABLE IF EXISTS product_variant_attributes CASCADE;
DROP TABLE IF EXISTS product_images CASCADE;
DROP TABLE IF EXISTS product_attributes CASCADE;
DROP TABLE IF EXISTS product_collections CASCADE;
DROP TABLE IF EXISTS product_variants CASCADE;
DROP TABLE IF EXISTS products CASCADE;
DROP TABLE IF EXISTS collections CASCADE;
DROP TABLE IF EXISTS categories CASCADE;

-- Supprimer les tables d'audit
DROP TABLE IF EXISTS product_audit_logs CASCADE;
DROP TABLE IF EXISTS category_audit_logs CASCADE;
DROP TABLE IF EXISTS collection_audit_logs CASCADE;

-- Supprimer la table outbox_events
DROP TABLE IF EXISTS outbox_events CASCADE;

-- Supprimer la table flyway_schema_history
DROP TABLE IF EXISTS flyway_schema_history CASCADE;

-- Réactiver les contraintes de clés étrangères
SET session_replication_role = DEFAULT;

-- Vérifier qu'il ne reste plus de tables
SELECT table_name 
FROM information_schema.tables 
WHERE table_schema = 'public' 
AND table_type = 'BASE TABLE';

-- Message de confirmation
DO $$
BEGIN
    RAISE NOTICE 'Base de données product_db nettoyée avec succès!';
END $$; 