-- Внешние ключи для таблицы ads
ALTER TABLE ads
    ADD CONSTRAINT fk_ads_author
        FOREIGN KEY (author_id)
            REFERENCES users(id)
            ON DELETE CASCADE;

ALTER TABLE ads
    ADD CONSTRAINT fk_ads_image
        FOREIGN KEY (image_id)
            REFERENCES images(id)
            ON DELETE SET NULL;

-- Внешние ключи для таблицы comments
ALTER TABLE comments
    ADD CONSTRAINT fk_comments_author
        FOREIGN KEY (author_id)
            REFERENCES users(id)
            ON DELETE CASCADE;

ALTER TABLE comments
    ADD CONSTRAINT fk_comments_ad
        FOREIGN KEY (ad_id)
            REFERENCES ads(id)
            ON DELETE CASCADE;

-- Внешний ключ для аватаров пользователей
ALTER TABLE users
    ADD CONSTRAINT fk_users_image
        FOREIGN KEY (image_id)
            REFERENCES images(id)
            ON DELETE SET NULL;

-- Добавление индексов для улучшения производительности
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_ads_author_id ON ads(author_id);
CREATE INDEX IF NOT EXISTS idx_ads_created_at ON ads(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_comments_ad_id ON comments(ad_id);
CREATE INDEX IF NOT EXISTS idx_comments_created_at ON comments(created_at DESC);