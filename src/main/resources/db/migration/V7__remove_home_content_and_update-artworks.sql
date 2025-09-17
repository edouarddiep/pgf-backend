-- Migration V7 : Many-to-Many et suppression home_content

-- 1. Supprimer home_content
DROP TABLE IF EXISTS home_content CASCADE;

-- 2. Table de jointure Many-to-Many
CREATE TABLE IF NOT EXISTS artwork_categories_mapping (
                                                          artwork_id BIGINT NOT NULL,
                                                          category_id BIGINT NOT NULL,
                                                          PRIMARY KEY (artwork_id, category_id),
    CONSTRAINT fk_artwork_categories_artwork
    FOREIGN KEY (artwork_id) REFERENCES artworks(id) ON DELETE CASCADE,
    CONSTRAINT fk_artwork_categories_category
    FOREIGN KEY (category_id) REFERENCES artwork_categories(id) ON DELETE CASCADE
    );

-- 3. Migrer les données existantes (seulement si category_id existe encore)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_name = 'artworks' AND column_name = 'category_id') THEN
        INSERT INTO artwork_categories_mapping (artwork_id, category_id)
SELECT id, category_id FROM artworks WHERE category_id IS NOT NULL
    ON CONFLICT DO NOTHING;
END IF;
END $$;

-- 4. Supprimer l'ancienne colonne category_id si elle existe
ALTER TABLE artworks DROP COLUMN IF EXISTS category_id;

-- 5. Ajouter les nouvelles colonnes seulement si elles n'existent pas
DO $$
BEGIN
    -- Dimensions
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_name = 'artworks' AND column_name = 'dimensions') THEN
ALTER TABLE artworks ADD COLUMN dimensions VARCHAR(255);
END IF;

    -- Materials
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_name = 'artworks' AND column_name = 'materials') THEN
ALTER TABLE artworks ADD COLUMN materials VARCHAR(255);
END IF;

    -- Creation date
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_name = 'artworks' AND column_name = 'creation_date') THEN
ALTER TABLE artworks ADD COLUMN creation_date DATE;
END IF;

    -- Price
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_name = 'artworks' AND column_name = 'price') THEN
ALTER TABLE artworks ADD COLUMN price NUMERIC(10,2);
END IF;

    -- Main image URL
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_name = 'artworks' AND column_name = 'main_image_url') THEN
ALTER TABLE artworks ADD COLUMN main_image_url VARCHAR(500);
END IF;
END $$;