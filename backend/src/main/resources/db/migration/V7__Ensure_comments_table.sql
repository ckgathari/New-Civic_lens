-- V7__Ensure_all_tables.sql
-- Idempotent repair migration: ensures every table exists regardless of
-- prior Flyway history state (handles partial/failed V1 runs on fresh DBs).

-- Base lookup tables (no dependencies)
CREATE TABLE IF NOT EXISTS counties (
    id INTEGER PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS constituencies (
    id INTEGER PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    county_id INTEGER NOT NULL,
    FOREIGN KEY (county_id) REFERENCES counties(id)
);

CREATE TABLE IF NOT EXISTS wards (
    id INTEGER PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    constituency_id INTEGER NOT NULL,
    FOREIGN KEY (constituency_id) REFERENCES constituencies(id)
);

-- Users
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(255) PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    phone VARCHAR(255),
    national_id VARCHAR(255),
    is_leader BOOLEAN DEFAULT FALSE,
    is_aspirant BOOLEAN DEFAULT FALSE
);

-- Leaders
CREATE TABLE IF NOT EXISTS leaders (
    id VARCHAR(255) PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255),
    email VARCHAR(255) UNIQUE,
    phone_number VARCHAR(255),
    national_id VARCHAR(255),
    position VARCHAR(255) NOT NULL,
    photo_url VARCHAR(255),
    county_id INTEGER,
    constituency_id INTEGER,
    ward_id INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (county_id) REFERENCES counties(id),
    FOREIGN KEY (constituency_id) REFERENCES constituencies(id),
    FOREIGN KEY (ward_id) REFERENCES wards(id)
);

-- Comments
CREATE TABLE IF NOT EXISTS comments (
    id VARCHAR(255) PRIMARY KEY,
    comment TEXT NOT NULL,
    rating INTEGER,
    type VARCHAR(50),
    parent_id VARCHAR(255),
    leader_id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    hidden BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (leader_id) REFERENCES leaders(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_id) REFERENCES comments(id) ON DELETE CASCADE
);

-- Poll votes (V3)
CREATE TABLE IF NOT EXISTS poll_votes (
    id VARCHAR(36) PRIMARY KEY,
    voter_id VARCHAR(255) NOT NULL,
    leader_candidate_id VARCHAR(36),
    aspirant_candidate_id VARCHAR(36),
    candidate_type VARCHAR(50) NOT NULL DEFAULT 'LEADER',
    position VARCHAR(255) NOT NULL,
    county_id INTEGER,
    constituency_id INTEGER,
    ward_id INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (voter_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (leader_candidate_id) REFERENCES leaders(id) ON DELETE CASCADE,
    FOREIGN KEY (county_id) REFERENCES counties(id) ON DELETE CASCADE,
    FOREIGN KEY (constituency_id) REFERENCES constituencies(id) ON DELETE CASCADE,
    FOREIGN KEY (ward_id) REFERENCES wards(id) ON DELETE CASCADE,
    UNIQUE(voter_id, position, county_id, constituency_id, ward_id)
);

CREATE INDEX IF NOT EXISTS idx_poll_votes_position_location ON poll_votes(position, county_id, constituency_id, ward_id);
CREATE INDEX IF NOT EXISTS idx_poll_votes_leader_candidate_id ON poll_votes(leader_candidate_id);
CREATE INDEX IF NOT EXISTS idx_poll_votes_aspirant_candidate_id ON poll_votes(aspirant_candidate_id);
CREATE INDEX IF NOT EXISTS idx_poll_votes_voter ON poll_votes(voter_id);
CREATE INDEX IF NOT EXISTS idx_poll_votes_candidate_type ON poll_votes(candidate_type);
CREATE INDEX IF NOT EXISTS idx_poll_votes_created_at ON poll_votes(created_at);

-- Aspirants (V4)
CREATE TABLE IF NOT EXISTS aspirants (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20) NOT NULL DEFAULT '',
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

CREATE INDEX IF NOT EXISTS idx_aspirants_position ON aspirants(position);
CREATE INDEX IF NOT EXISTS idx_aspirants_position_location ON aspirants(position, county_id, constituency_id, ward_id);
CREATE INDEX IF NOT EXISTS idx_aspirants_county ON aspirants(county_id);
CREATE INDEX IF NOT EXISTS idx_aspirants_email ON aspirants(email);
CREATE INDEX IF NOT EXISTS idx_aspirants_created_at ON aspirants(created_at);
