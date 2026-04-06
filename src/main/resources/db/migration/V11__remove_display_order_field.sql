ALTER TABLE artworks DROP COLUMN IF EXISTS display_order;
ALTER TABLE artwork_categories DROP COLUMN IF EXISTS display_order;
ALTER TABLE archives DROP COLUMN IF EXISTS display_order;
ALTER TABLE archive_files DROP COLUMN IF EXISTS display_order;

DROP INDEX IF EXISTS idx_artworks_display_order;
DROP INDEX IF EXISTS idx_archives_display_order;
DROP INDEX IF EXISTS idx_archive_files_display_order;