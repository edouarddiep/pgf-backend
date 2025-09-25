ALTER TABLE artwork_categories
    ADD COLUMN main_image_url VARCHAR(500),
ADD COLUMN thumbnail_url VARCHAR(500);

-- Ajouter des commentaires pour documenter les nouveaux champs
COMMENT ON COLUMN artwork_categories.main_image_url IS 'URL de l''image principale de la catégorie';
COMMENT ON COLUMN artwork_categories.thumbnail_url IS 'URL de la miniature de la catégorie pour affichage liste';