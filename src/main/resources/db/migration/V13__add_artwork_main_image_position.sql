ALTER TABLE artworks
    ADD COLUMN main_image_position_x INTEGER DEFAULT 50,
    ADD COLUMN main_image_position_y INTEGER DEFAULT 50,
    ADD COLUMN main_image_zoom INTEGER DEFAULT 100;