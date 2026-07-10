-- V1__Create_schema.sql
-- Create tables based on JPA entities

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