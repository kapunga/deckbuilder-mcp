-- Create schema version tracking table
CREATE TABLE IF NOT EXISTS schema_version (
    version INTEGER PRIMARY KEY,
    applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    script_name TEXT NOT NULL
);

-- Insert initial version record
INSERT OR IGNORE INTO schema_version (version, script_name)
VALUES (1, '001_schema_version.sql');