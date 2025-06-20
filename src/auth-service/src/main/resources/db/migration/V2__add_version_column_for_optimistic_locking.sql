-- Add version column for optimistic locking
ALTER TABLE authentications
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;


