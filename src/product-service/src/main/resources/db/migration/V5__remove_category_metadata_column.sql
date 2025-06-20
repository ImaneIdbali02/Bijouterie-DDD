-- Migration V5: Suppression de la colonne metadata de la table categories
ALTER TABLE categories DROP COLUMN IF EXISTS metadata; 