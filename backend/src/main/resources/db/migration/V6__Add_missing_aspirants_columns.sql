-- Add missing columns to aspirants table if they don't exist
-- Idempotent migration that safely adds columns one by one

-- Add bio column if missing
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns 
    WHERE table_name = 'aspirants' AND column_name = 'bio'
  ) THEN
    ALTER TABLE aspirants ADD COLUMN bio TEXT;
  END IF;
END
$$;

-- Add manifesto column if missing
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns 
    WHERE table_name = 'aspirants' AND column_name = 'manifesto'
  ) THEN
    ALTER TABLE aspirants ADD COLUMN manifesto TEXT;
  END IF;
END
$$;

-- Add profile_picture column if missing
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns 
    WHERE table_name = 'aspirants' AND column_name = 'profile_picture'
  ) THEN
    ALTER TABLE aspirants ADD COLUMN profile_picture VARCHAR(500);
  END IF;
END
$$;

-- Add phone_number column if missing
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns 
    WHERE table_name = 'aspirants' AND column_name = 'phone_number'
  ) THEN
    ALTER TABLE aspirants ADD COLUMN phone_number VARCHAR(20) DEFAULT '';
  END IF;
END
$$;

-- Add updated_at column if missing
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns 
    WHERE table_name = 'aspirants' AND column_name = 'updated_at'
  ) THEN
    ALTER TABLE aspirants ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
  END IF;
END
$$;

-- Ensure NOT NULL constraints are set properly
DO $$
BEGIN
  -- Set NOT NULL on phone_number if column exists and allows nulls
  IF EXISTS (
    SELECT 1 FROM information_schema.columns 
    WHERE table_name = 'aspirants' AND column_name = 'phone_number'
  ) THEN
    ALTER TABLE aspirants ALTER COLUMN phone_number SET NOT NULL;
  END IF;
  
  -- Set NOT NULL on updated_at if column exists and allows nulls
  IF EXISTS (
    SELECT 1 FROM information_schema.columns 
    WHERE table_name = 'aspirants' AND column_name = 'updated_at'
  ) THEN
    ALTER TABLE aspirants ALTER COLUMN updated_at SET NOT NULL;
  END IF;
END
$$;
