CREATE TABLE IF NOT EXISTS exhibition_files (
    id             BIGSERIAL PRIMARY KEY,
    exhibition_id  BIGINT      NOT NULL REFERENCES exhibitions (id) ON DELETE CASCADE,
    media_type     VARCHAR(50) NOT NULL,
    file_type      VARCHAR(50) NOT NULL,
    file_url       TEXT        NOT NULL,
    thumbnail_url  TEXT,
    file_name      VARCHAR(255),
    mime_type      VARCHAR(100),
    file_size      BIGINT,
    title          VARCHAR(255),
    title_en       VARCHAR(255),
    description    TEXT,
    description_en TEXT,
    source         VARCHAR(255),
    published_on   DATE,
    display_order  INTEGER     NOT NULL DEFAULT 0,
    created_at     TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_exhibition_files_exhibition ON exhibition_files (exhibition_id, display_order);
CREATE INDEX IF NOT EXISTS idx_exhibition_files_media_type ON exhibition_files (exhibition_id, media_type);

INSERT INTO exhibition_files (exhibition_id, media_type, file_type, file_url, display_order)
SELECT e.id, 'PHOTO', 'IMAGE', media.url, (media.ord - 1)::INTEGER
FROM exhibitions e,
     unnest(e.image_urls) WITH ORDINALITY AS media(url, ord)
WHERE media.url IS NOT NULL
  AND media.url <> '';

INSERT INTO exhibition_files (exhibition_id, media_type, file_type, file_url, display_order)
SELECT e.id, 'VIDEO', 'VIDEO', media.url, (media.ord - 1)::INTEGER
FROM exhibitions e,
     unnest(e.video_urls) WITH ORDINALITY AS media(url, ord)
WHERE media.url IS NOT NULL
  AND media.url <> '';
