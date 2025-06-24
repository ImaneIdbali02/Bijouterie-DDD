-- Script pour créer une catégorie de test
INSERT INTO categories (
    id,
    name,
    description,
    parent_id,
    level,
    path,
    active,
    display_order,
    creation_date,
    modification_date,
    version,
    slug,
    visible_in_menu
) VALUES (
    'dee1b441-0e9b-4706-b93d-ed935e9545e4', -- ID spécifique utilisé dans l'erreur
    'Bijoux en Or',
    'Collection de bijoux en or pur de haute qualité',
    NULL, -- Pas de parent
    0, -- Niveau racine
    '/bijoux-en-or',
    true,
    1,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0,
    'bijoux-en-or',
    true
);

-- Vérification
SELECT id, name, description, active FROM categories WHERE id = 'dee1b441-0e9b-4706-b93d-ed935e9545e4'; 