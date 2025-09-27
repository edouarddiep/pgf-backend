CREATE TABLE vernissage_registrations (
                                          id BIGSERIAL PRIMARY KEY,
                                          exhibition_id BIGINT NOT NULL,
                                          first_name VARCHAR(255) NOT NULL,
                                          last_name VARCHAR(255) NOT NULL,
                                          email VARCHAR(255) NOT NULL,
                                          phone VARCHAR(50),
                                          number_of_guests INTEGER NOT NULL DEFAULT 1,
                                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                          updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                          CONSTRAINT fk_vernissage_registrations_exhibition
                                              FOREIGN KEY (exhibition_id) REFERENCES exhibitions(id) ON DELETE CASCADE
);