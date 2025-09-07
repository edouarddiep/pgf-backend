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
                          dimensions VARCHAR(100),
                          materials VARCHAR(255),
                          creation_date DATE,
                          price DECIMAL(10, 2),
                          is_available BOOLEAN DEFAULT TRUE,
                          image_url VARCHAR(500),
                          thumbnail_url VARCHAR(500),
                          display_order INTEGER DEFAULT 0,
                          category_id BIGINT NOT NULL REFERENCES artwork_categories(id) ON DELETE CASCADE,
                          created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Table des expositions
CREATE TABLE exhibitions (
                             id BIGSERIAL PRIMARY KEY,
                             title VARCHAR(255) NOT NULL,
                             description TEXT,
                             location VARCHAR(255),
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
CREATE INDEX idx_artworks_category_id ON artworks(category_id);
CREATE INDEX idx_artworks_is_available ON artworks(is_available);
CREATE INDEX idx_artworks_display_order ON artworks(display_order);
CREATE INDEX idx_artwork_categories_slug ON artwork_categories(slug);
CREATE INDEX idx_exhibitions_status ON exhibitions(status);
CREATE INDEX idx_exhibitions_start_date ON exhibitions(start_date);
CREATE INDEX idx_contact_messages_status ON contact_messages(status);
CREATE INDEX idx_contact_messages_is_read ON contact_messages(is_read);

-- Insertion des catégories d'œuvres initiales
INSERT INTO artwork_categories (name, description, slug, display_order) VALUES
                                                                            ('Fils de fer', 'Œuvres réalisées avec des fils de fer', 'fils-de-fer', 1),
                                                                            ('Toile de Jute', 'Créations sur toile de jute', 'toile-de-jute', 2),
                                                                            ('Peinture', 'Tableaux et peintures', 'peinture', 3),
                                                                            ('Sculpture', 'Sculptures et installations', 'sculpture', 4),
                                                                            ('Écriture', 'Textes et œuvres littéraires', 'ecriture', 5);