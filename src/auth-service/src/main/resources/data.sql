INSERT INTO authentications (
    id, client_id, created_at, email, enabled, locked, password_hash,
    password_reset_token, password_reset_token_expiry, provider, provider_user_id,
    role, updated_at, username, version
) VALUES (
             'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
             'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12',
             NOW(),
             'enaya@gmail.com',
             true,
             false,
             '$2a$12$6JoSuL05pX0Z1tXZ4oCV9u1dR9r6xVUMUEmvimr2kqovOKOAW9V.W',
             null,
             null,
             'LOCAL',
             null,
             'ADMIN',
             NOW(),
             'admin',
             0 --
         );

UPDATE authentications SET role = 'ROLE_ADMIN' WHERE email = 'enaya@gmail.com';