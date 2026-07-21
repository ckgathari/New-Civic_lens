-- V7__Ensure_comments_table.sql
-- Ensures the comments table exists in case V1 was partially applied

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
