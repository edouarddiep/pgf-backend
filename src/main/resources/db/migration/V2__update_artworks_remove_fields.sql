-- Migration pour supprimer les champs inutiles et ajouter la gestion multi-images

-- 1. Supprimer les colonnes non nécessaires
ALTER TABLE artworks DROP COLUMN IF EXISTS dimensions;
ALTER TABLE artworks DROP COLUMN IF EXISTS materials;
ALTER TABLE artworks DROP COLUMN IF EXISTS creation_date;
ALTER TABLE artworks DROP COLUMN IF EXISTS price;

-- 2. Supprimer les anciennes colonnes d'images (si elles existent)
ALTER TABLE artworks DROP COLUMN IF EXISTS image_url;
ALTER TABLE artworks DROP COLUMN IF EXISTS thumbnail_url;

-- 3. Créer la table pour les images multiples
CREATE TABLE IF NOT EXISTS artwork_images (
                                              artwork_id BIGINT NOT NULL,
                                              image_url VARCHAR(500) NOT NULL,
    CONSTRAINT fk_artwork_images_artwork FOREIGN KEY (artwork_id) REFERENCES artworks(id) ON DELETE CASCADE
    );

-- 4. Créer un index pour améliorer les performances
CREATE INDEX IF NOT EXISTS idx_artwork_images_artwork_id ON artwork_images(artwork_id);

-- 5. Mettre à jour les œuvres existantes avec des valeurs par défaut si nécessaire
UPDATE artworks SET is_available = true WHERE is_available IS NULL;
UPDATE artworks SET display_order = 0 WHERE display_order IS NULL;