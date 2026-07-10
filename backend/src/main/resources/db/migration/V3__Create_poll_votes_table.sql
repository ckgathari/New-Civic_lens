-- Create poll_votes table for popularity polls
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

    -- Foreign keys
    FOREIGN KEY (voter_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (leader_candidate_id) REFERENCES leaders(id) ON DELETE CASCADE,
    FOREIGN KEY (county_id) REFERENCES counties(id) ON DELETE CASCADE,
    FOREIGN KEY (constituency_id) REFERENCES constituencies(id) ON DELETE CASCADE,
    FOREIGN KEY (ward_id) REFERENCES wards(id) ON DELETE CASCADE,

    -- Ensure one vote per user per position per location
    UNIQUE(voter_id, position, county_id, constituency_id, ward_id)
);

-- Create indexes for better query performance (IF NOT EXISTS syntax varies by database)
CREATE INDEX IF NOT EXISTS idx_poll_votes_position_location ON poll_votes(position, county_id, constituency_id, ward_id);
CREATE INDEX IF NOT EXISTS idx_poll_votes_leader_candidate_id ON poll_votes(leader_candidate_id);
CREATE INDEX IF NOT EXISTS idx_poll_votes_aspirant_candidate_id ON poll_votes(aspirant_candidate_id);
CREATE INDEX IF NOT EXISTS idx_poll_votes_voter ON poll_votes(voter_id);
CREATE INDEX IF NOT EXISTS idx_poll_votes_candidate_type ON poll_votes(candidate_type);
CREATE INDEX IF NOT EXISTS idx_poll_votes_created_at ON poll_votes(created_at);