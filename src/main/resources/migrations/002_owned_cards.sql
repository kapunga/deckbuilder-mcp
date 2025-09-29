-- Create owned cards table
CREATE TABLE IF NOT EXISTS owned_cards (
    set_id TEXT NOT NULL,
    set_number INTEGER NOT NULL,
    owned BOOLEAN NOT NULL DEFAULT 0,
    owned_count INTEGER NULL,
    interested BOOLEAN NOT NULL DEFAULT 0,
    PRIMARY KEY (set_id, set_number)
);

-- Record this migration
INSERT OR IGNORE INTO schema_version (version, script_name)
VALUES (2, '002_owned_cards.sql');