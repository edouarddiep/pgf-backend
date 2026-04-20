ALTER TABLE artworks
    ADD COLUMN title_en VARCHAR(255),
    ADD COLUMN description_en TEXT;

ALTER TABLE artwork_categories
    ADD COLUMN name_en VARCHAR(255),
    ADD COLUMN description_en TEXT;

ALTER TABLE exhibitions
    ADD COLUMN title_en VARCHAR(255),
    ADD COLUMN description_en TEXT;

ALTER TABLE archives
    ADD COLUMN title_en VARCHAR(255),
    ADD COLUMN description_en TEXT;