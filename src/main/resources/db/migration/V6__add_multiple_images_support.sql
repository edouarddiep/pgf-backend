-- Migration V6 : Support des images multiples et nouvelles catégories

-- 1. Ajouter les colonnes pour images multiples dans la table artworks
ALTER TABLE artworks
    ADD COLUMN IF NOT EXISTS image_urls TEXT[], -- Array d'URLs d'images
    ADD COLUMN IF NOT EXISTS main_image_url VARCHAR(500); -- URL de l'image principale

-- 2. Migrer les données existantes de artwork_images vers image_urls (si artwork_images existe et contient des données)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'artwork_images') THEN
        -- Regrouper les images par artwork_id et créer un array
UPDATE artworks
SET image_urls = (
    SELECT ARRAY_AGG(ai.image_url ORDER BY ai.image_url)
    FROM artwork_images ai
    WHERE ai.artwork_id = artworks.id
),
    main_image_url = (
        SELECT ai.image_url
        FROM artwork_images ai
        WHERE ai.artwork_id = artworks.id
    LIMIT 1
    )
WHERE EXISTS (
    SELECT 1 FROM artwork_images ai WHERE ai.artwork_id = artworks.id
    );
END IF;
END $$;

-- 3. Supprimer la table artwork_images (remplacée par les colonnes image_urls et main_image_url)
DROP TABLE IF EXISTS artwork_images;

-- 4. Supprimer les anciennes catégories et insérer les nouvelles
-- D'abord supprimer les œuvres liées aux anciennes catégories (optionnel, à décommenter si souhaité)
-- DELETE FROM artworks WHERE category_id IN (
--     SELECT id FROM artwork_categories WHERE slug IN ('toile-de-jute', 'peinture', 'sculpture', 'ecriture')
-- );

-- Supprimer les anciennes catégories
DELETE FROM artwork_categories WHERE slug IN ('toile-de-jute', 'peinture', 'sculpture', 'ecriture');

-- Mettre à jour les catégories existantes
UPDATE artwork_categories
SET name = 'Sculptures et bronze',
    slug = 'sculptures-et-bronze',
    description = 'Sculptures réalisées en bronze et autres matériaux',
    display_order = 1
WHERE slug = 'fils-de-fer' AND EXISTS (
    SELECT 1 FROM artwork_categories WHERE slug = 'fils-de-fer'
);

-- Insérer les nouvelles catégories
INSERT INTO artwork_categories (name, description, slug, display_order) VALUES
                                                                            ('Sculptures et bronze', 'Sculptures réalisées en bronze et autres matériaux', 'sculptures-et-bronze', 1),
                                                                            ('Fils de fer', 'Créations artistiques en fil de fer', 'fils-de-fer', 2),
                                                                            ('Toiles de jute - Tableaux', 'Tableaux sur toile de jute', 'toiles-de-jute-tableaux', 3),
                                                                            ('Toiles de jute - Masques', 'Masques artistiques sur toile de jute', 'toiles-de-jute-masques', 4),
                                                                            ('Toiles de jute - Pélerins', 'Série des pélerins sur toile de jute', 'toiles-de-jute-pelerins', 5),
                                                                            ('Toiles de jute - Totems', 'Totems sur toile de jute', 'toiles-de-jute-totems', 6),
                                                                            ('Collages', 'Œuvres de collage mixte', 'collages', 7),
                                                                            ('Dessins', 'Dessins et esquisses', 'dessins', 8),
                                                                            ('Papiers japonais', 'Créations sur papiers japonais', 'papiers-japonais', 9),
                                                                            ('Land art', 'Installations dans la nature', 'land-art', 10),
                                                                            ('Sacs - colliers', 'Accessoires artistiques', 'sacs-colliers', 11),
                                                                            ('Tissage - textile', 'Créations textiles et tissages', 'tissage-textile', 12),
                                                                            ('Cléo', 'Série Cléo', 'cleo', 13)
    ON CONFLICT (slug) DO UPDATE SET
    name = EXCLUDED.name,
                              description = EXCLUDED.description,
                              display_order = EXCLUDED.display_order;

-- 5. Ajout des index pour les nouvelles colonnes
CREATE INDEX IF NOT EXISTS idx_artworks_main_image_url ON artworks(main_image_url);
CREATE INDEX IF NOT EXISTS idx_artworks_image_urls ON artworks USING GIN(image_urls);

-- 6. Nettoyer les valeurs NULL et définir des valeurs par défaut cohérentes
UPDATE artworks SET
                    is_available = COALESCE(is_available, true),
                    display_order = COALESCE(display_order, 0),
                    image_urls = COALESCE(image_urls, ARRAY[]::TEXT[])
WHERE is_available IS NULL OR display_order IS NULL OR image_urls IS NULL;