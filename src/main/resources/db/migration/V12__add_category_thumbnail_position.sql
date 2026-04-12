ALTER TABLE artwork_categories
    ADD COLUMN thumbnail_position_x INTEGER DEFAULT 50,
    ADD COLUMN thumbnail_position_y INTEGER DEFAULT 50,
    ADD COLUMN thumbnail_zoom INTEGER DEFAULT 100;