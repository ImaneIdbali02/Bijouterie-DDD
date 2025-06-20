-- Fix product_variants table by adding missing columns
-- Run this script manually in your PostgreSQL database

-- Add creation_date column if it doesn't exist
DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_name = 'product_variants' 
        AND column_name = 'creation_date'
    ) THEN
        ALTER TABLE product_variants ADD COLUMN creation_date TIMESTAMP WITHOUT TIME ZONE;
        UPDATE product_variants SET creation_date = CURRENT_TIMESTAMP WHERE creation_date IS NULL;
        ALTER TABLE product_variants ALTER COLUMN creation_date SET NOT NULL;
        RAISE NOTICE 'Added creation_date column to product_variants';
    ELSE
        RAISE NOTICE 'creation_date column already exists in product_variants';
    END IF;
END $$;

-- Add modification_date column if it doesn't exist
DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_name = 'product_variants' 
        AND column_name = 'modification_date'
    ) THEN
        ALTER TABLE product_variants ADD COLUMN modification_date TIMESTAMP WITHOUT TIME ZONE;
        UPDATE product_variants SET modification_date = CURRENT_TIMESTAMP WHERE modification_date IS NULL;
        ALTER TABLE product_variants ALTER COLUMN modification_date SET NOT NULL;
        RAISE NOTICE 'Added modification_date column to product_variants';
    ELSE
        RAISE NOTICE 'modification_date column already exists in product_variants';
    END IF;
END $$;

-- Verify the columns exist
SELECT column_name, data_type, is_nullable 
FROM information_schema.columns 
WHERE table_name = 'product_variants' 
AND column_name IN ('creation_date', 'modification_date')
ORDER BY column_name; 