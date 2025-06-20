ALTER TABLE authentications
ADD COLUMN IF NOT EXISTS password_reset_token VARCHAR(255),
ADD COLUMN IF NOT EXISTS password_reset_token_expiry TIMESTAMP;

CREATE INDEX IF NOT EXISTS idx_auth_reset_token ON authentications (password_reset_token);

CREATE INDEX IF NOT EXISTS idx_auth_reset_token_expiry ON authentications (password_reset_token_expiry); 