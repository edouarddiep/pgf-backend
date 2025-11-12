CREATE TABLE IF NOT EXISTS archives (
                                        id BIGSERIAL PRIMARY KEY,
                                        title VARCHAR(255) NOT NULL,
    year INTEGER NOT NULL,
    description TEXT,
    thumbnail_url TEXT,
    display_order INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS archive_files (
                                             id BIGSERIAL PRIMARY KEY,
                                             archive_id BIGINT NOT NULL,
                                             file_type VARCHAR(50) NOT NULL,
    file_url TEXT NOT NULL,
    file_name VARCHAR(255),
    display_order INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (archive_id) REFERENCES archives(id) ON DELETE CASCADE
    );

CREATE INDEX idx_archives_year ON archives(year DESC);
CREATE INDEX idx_archives_display_order ON archives(display_order);
CREATE INDEX idx_archive_files_archive_id ON archive_files(archive_id);
CREATE INDEX idx_archive_files_display_order ON archive_files(display_order);