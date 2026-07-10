-- V2__Load_initial_data.sql
-- Load data from CSV files (convert CSVs to INSERT statements)
-- Note: In a real scenario, use a tool to convert CSVs to SQL INSERTs
-- For example, run a script to generate INSERTs from your CSVs

-- Sample inserts (replace with actual data from CSVs)
-- Counties
INSERT INTO counties (id, name) VALUES (1, 'MOMBASA'), (2, 'KWALE');

-- Constituencies
INSERT INTO constituencies (id, name, county_id) VALUES (1, 'CHANGAMWE', 1), (2, 'JOMVU', 1);

-- Wards
INSERT INTO wards (id, name, constituency_id) VALUES (1, 'PORT REITZ', 1), (2, 'KIPEVU', 1);

-- Users (hashed passwords)
INSERT INTO users (id, email, password, first_name, last_name, phone, national_id, is_leader, is_aspirant)
VALUES ('user1', 'user@example.com', '$2a$10$...', 'John', 'Doe', '123456789', '12345678', false, false);

-- Leaders
INSERT INTO leaders (id, first_name, last_name, position, county_id, constituency_id, ward_id)
VALUES ('leader1', 'Jane', 'Smith', 'MP', 1, 1, 1);

-- Add more INSERTs based on your CSVs