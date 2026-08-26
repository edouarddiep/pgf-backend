ALTER TABLE exhibition_files
    ADD COLUMN IF NOT EXISTS source_url TEXT;
