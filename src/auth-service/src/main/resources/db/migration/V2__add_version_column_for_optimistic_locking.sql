-- Add version column for optimistic locking
ALTER TABLE authentications
    ADD COLUMN version BIGINT DEFAULT 0;

-- Update existing records to have version = 0
UPDATE authentications
SET version = 0
WHERE version IS NULL;