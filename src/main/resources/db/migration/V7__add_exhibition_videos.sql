ALTER TABLE exhibitions ADD COLUMN IF NOT EXISTS video_urls TEXT[];

CREATE INDEX IF NOT EXISTS idx_exhibitions_video_urls ON exhibitions USING GIN(video_urls);