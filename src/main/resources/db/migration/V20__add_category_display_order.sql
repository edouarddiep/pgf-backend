ALTER TABLE artwork_categories ADD COLUMN IF NOT EXISTS display_order INTEGER DEFAULT 0;

UPDATE artwork_categories SET display_order = CASE name
                                                  WHEN 'Toile de jute'    THEN 1
                                                  WHEN 'Fils de fer'      THEN 2
                                                  WHEN 'Papier japonais'  THEN 3
                                                  WHEN 'Collages'         THEN 4
                                                  WHEN 'Peintures'        THEN 5
                                                  WHEN 'Sculptures'       THEN 6
                                                  WHEN 'Land art'         THEN 7
                                                  WHEN 'Livres et objets' THEN 8
                                                  WHEN 'Sacs et colliers' THEN 9
                                                  ELSE 99
    END;

CREATE INDEX IF NOT EXISTS idx_artwork_categories_display_order ON artwork_categories(display_order);