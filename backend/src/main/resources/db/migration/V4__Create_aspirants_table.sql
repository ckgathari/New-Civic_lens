-- Create aspirants table with enhanced fields
CREATE TABLE IF NOT EXISTS aspirants (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    position VARCHAR(255) NOT NULL,
    bio TEXT,
    manifesto TEXT,
    profile_picture VARCHAR(500),
    county_id INTEGER NOT NULL REFERENCES counties(id) ON DELETE CASCADE,
    constituency_id INTEGER REFERENCES constituencies(id) ON DELETE SET NULL,
    ward_id INTEGER REFERENCES wards(id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for aspirants
CREATE INDEX IF NOT EXISTS idx_aspirants_position ON aspirants(position);
CREATE INDEX IF NOT EXISTS idx_aspirants_position_location ON aspirants(position, county_id, constituency_id, ward_id);
CREATE INDEX IF NOT EXISTS idx_aspirants_county ON aspirants(county_id);
CREATE INDEX IF NOT EXISTS idx_aspirants_email ON aspirants(email);
CREATE INDEX IF NOT EXISTS idx_aspirants_created_at ON aspirants(created_at);