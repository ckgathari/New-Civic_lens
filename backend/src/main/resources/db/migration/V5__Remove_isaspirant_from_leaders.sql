-- Remove is_aspirant column from leaders table
ALTER TABLE leaders DROP COLUMN IF EXISTS is_aspirant;