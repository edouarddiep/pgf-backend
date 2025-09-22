-- Migration initiale pour le projet PGF Artist Website

-- Table des catégories d'œuvres
CREATE TABLE artwork_categories (
                                    id BIGSERIAL PRIMARY KEY,
                                    name VARCHAR(100) NOT NULL UNIQUE,
                                    description TEXT,
                                    slug VARCHAR(100) NOT NULL UNIQUE,
                                    display_order INTEGER DEFAULT 0,
                                    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Table des œuvres d'art
CREATE TABLE artworks (
                          id BIGSERIAL PRIMARY KEY,
                          title VARCHAR(255) NOT NULL,
                          description TEXT,
                          dimensions VARCHAR(255),
                          materials VARCHAR(255),
                          creation_date DATE,
                          price NUMERIC(10,2),
                          is_available BOOLEAN DEFAULT TRUE,
                          display_order INTEGER DEFAULT 0,
                          image_urls TEXT[],
                          main_image_url VARCHAR(500),
                          created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Table de jointure Many-to-Many pour artwork-categories
CREATE TABLE artwork_categories_mapping (
                                            artwork_id BIGINT NOT NULL,
                                            category_id BIGINT NOT NULL,
                                            PRIMARY KEY (artwork_id, category_id),
                                            CONSTRAINT fk_artwork_categories_artwork
                                                FOREIGN KEY (artwork_id) REFERENCES artworks(id) ON DELETE CASCADE,
                                            CONSTRAINT fk_artwork_categories_category
                                                FOREIGN KEY (category_id) REFERENCES artwork_categories(id) ON DELETE CASCADE
);

-- Table des expositions
CREATE TABLE exhibitions (
                             id BIGSERIAL PRIMARY KEY,
                             title VARCHAR(255) NOT NULL,
                             description TEXT,
                             location VARCHAR(255),
                             address VARCHAR(500),
                             start_date DATE,
                             end_date DATE,
                             image_url VARCHAR(500),
                             is_featured BOOLEAN DEFAULT FALSE,
                             status VARCHAR(20) DEFAULT 'UPCOMING' CHECK (status IN ('UPCOMING', 'ONGOING', 'PAST')),
                             created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                             updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Table des messages de contact
CREATE TABLE contact_messages (
                                  id BIGSERIAL PRIMARY KEY,
                                  name VARCHAR(100) NOT NULL,
                                  email VARCHAR(100) NOT NULL,
                                  phone VARCHAR(20),
                                  subject VARCHAR(200),
                                  message TEXT NOT NULL,
                                  is_read BOOLEAN DEFAULT FALSE,
                                  status VARCHAR(20) DEFAULT 'NEW' CHECK (status IN ('NEW', 'read', 'REPLIED', 'ARCHIVED')),
                                  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                  updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Index pour les performances
CREATE INDEX idx_artworks_is_available ON artworks(is_available);
CREATE INDEX idx_artworks_display_order ON artworks(display_order);
CREATE INDEX idx_artworks_main_image_url ON artworks(main_image_url);
CREATE INDEX idx_artworks_image_urls ON artworks USING GIN(image_urls);
CREATE INDEX idx_artwork_categories_slug ON artwork_categories(slug);
CREATE INDEX idx_artwork_categories_mapping_artwork ON artwork_categories_mapping(artwork_id);
CREATE INDEX idx_artwork_categories_mapping_category ON artwork_categories_mapping(category_id);
CREATE INDEX idx_exhibitions_status ON exhibitions(status);
CREATE INDEX idx_exhibitions_start_date ON exhibitions(start_date);
CREATE INDEX idx_contact_messages_status ON contact_messages(status);
CREATE INDEX idx_contact_messages_is_read ON contact_messages(is_read);

-- Insertion des catégories
INSERT INTO artwork_categories (name, slug, description, display_order) VALUES
                                                                            ('Collages & dessins', 'collages-dessins', 'Œuvres sur papier, collages et dessins', 1),
                                                                            ('Fils de fer', 'fils-de-fer', 'Sculptures et créations en fil de fer', 2),
                                                                            ('Land art', 'land-art', 'Créations artistiques dans et avec la nature', 3),
                                                                            ('Livres & objets', 'livres-objets', 'Livres d''artiste et objets créatifs', 4),
                                                                            ('Papiers japonais', 'papiers-japonais', 'Créations utilisant des papiers japonais traditionnels', 5),
                                                                            ('Peintures', 'peintures', 'Peintures sur toile et autres supports', 6),
                                                                            ('Sacs & colliers', 'sacs-colliers', 'Accessoires et bijoux créés à la main', 7),
                                                                            ('Sculptures', 'sculptures', 'Sculptures en différents matériaux', 8),
                                                                            ('Toiles de jute', 'toiles-jute', 'Œuvres réalisées sur toile de jute', 9);