ALTER TABLE exhibitions ADD COLUMN IF NOT EXISTS image_urls TEXT[];

UPDATE exhibitions
SET image_urls = ARRAY[image_url]::TEXT[]
WHERE image_url IS NOT NULL AND image_url != '';

CREATE INDEX IF NOT EXISTS idx_exhibitions_image_urls ON exhibitions USING GIN(image_urls);

ALTER TABLE exhibitions DROP COLUMN IF EXISTS url;
ALTER TABLE exhibitions DROP COLUMN IF EXISTS display_order;

DROP INDEX IF EXISTS idx_exhibitions_display_order;
DROP INDEX IF EXISTS idx_exhibitions_start_date_display_order;