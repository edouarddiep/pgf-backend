-- Migration pour ajouter les colonnes manquantes à la table artwork

-- Ajouter les colonnes manquantes si elles n'existent pas déjà
ALTER TABLE artworks
    ADD COLUMN IF NOT EXISTS dimensions VARCHAR(255),
    ADD COLUMN IF NOT EXISTS materials VARCHAR(255),
    ADD COLUMN IF NOT EXISTS creation_date DATE,
    ADD COLUMN IF NOT EXISTS price DECIMAL(10,2);

-- Créer la table pour les images des œuvres si elle n'existe pas
CREATE TABLE IF NOT EXISTS artwork_images (
                                              artwork_id BIGINT NOT NULL,
                                              image_url VARCHAR(500) NOT NULL,
    FOREIGN KEY (artwork_id) REFERENCES artworks(id) ON DELETE CASCADE
    );

-- Index pour optimiser les requêtes sur les images
CREATE INDEX IF NOT EXISTS idx_artwork_images_artwork_id ON artwork_images (artwork_id);