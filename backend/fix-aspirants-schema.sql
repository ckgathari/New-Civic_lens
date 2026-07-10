-- Direct fix for missing aspirants table columns
-- Run this if Flyway migrations don't apply automatically

ALTER TABLE aspirants ADD COLUMN IF NOT EXISTS phone_number VARCHAR(20) NOT NULL DEFAULT '';
ALTER TABLE aspirants ADD COLUMN IF NOT EXISTS bio TEXT;
ALTER TABLE aspirants ADD COLUMN IF NOT EXISTS manifesto TEXT;
ALTER TABLE aspirants ADD COLUMN IF NOT EXISTS profile_picture VARCHAR(500);
ALTER TABLE aspirants ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- Verify the table structure
-- SELECT column_name, data_type, is_nullable FROM information_schema.columns WHERE table_name='aspirants' ORDER BY ordinal_position;
