ALTER TABLE exhibitions
    ADD COLUMN credits TEXT,
ADD COLUMN website_url VARCHAR(500);

COMMENT ON COLUMN exhibitions.credits IS 'Crédits de l''exposition (photographe, etc.)';
COMMENT ON COLUMN exhibitions.website_url IS 'URL du site web de l''exposition';