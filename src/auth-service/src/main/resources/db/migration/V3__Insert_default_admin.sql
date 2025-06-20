-- Script de migration Flyway V2
-- Insérer un utilisateur admin par défaut

-- Générer un hash BCrypt pour le mot de passe "admin123"
-- Hash généré avec BCrypt strength 12
INSERT INTO authentications (
    id,
    client_id,
    username,
    email,
    password_hash,
    provider,
    enabled,
    locked,
    created_at,
    updated_at
) VALUES (
             gen_random_uuid(),
             gen_random_uuid(),
             'admin',
             'enaya@gmail.com',
             '$2a$12$LQFnJ1hQh.E9z4x5QFqNzuAYb.YrQQmrKr8z.qR7yJ5wP9oD8.G4a', -- Mot de passe: admin123
             'LOCAL',
             true,
             false,
             NOW(),
             NOW()
         ) ON CONFLICT (username) DO NOTHING;

-- Insérer un utilisateur test
INSERT INTO authentications (
    id,
    client_id,
    username,
    email,
    password_hash,
    provider,
    enabled,
    locked,
    created_at,
    updated_at
) VALUES (
             gen_random_uuid(),
             gen_random_uuid(),
             'testuser',
             'test@enaya.com',
             '$2a$12$LQFnJ1hQh.E9z4x5QFqNzuAYb.YrQQmrKr8z.qR7yJ5wP9oD8.G4a', -- Mot de passe: admin123
             'LOCAL',
             true,
             false,
             NOW(),
             NOW()
         ) ON CONFLICT (username) DO NOTHING;