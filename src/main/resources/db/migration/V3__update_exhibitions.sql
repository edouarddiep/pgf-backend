-- Suppression de la colonne is_featured
ALTER TABLE exhibitions DROP COLUMN IF EXISTS is_featured;

-- Ajout de la colonne display_order si elle n'existe pas
ALTER TABLE exhibitions ADD COLUMN IF NOT EXISTS display_order INTEGER DEFAULT 0;

-- Mise à jour des ordres d'affichage existants
UPDATE exhibitions SET display_order = id WHERE display_order IS NULL OR display_order = 0;

-- Index pour améliorer les performances
CREATE INDEX IF NOT EXISTS idx_exhibitions_display_order ON exhibitions(display_order);
CREATE INDEX IF NOT EXISTS idx_exhibitions_start_date_display_order ON exhibitions(start_date, display_order);