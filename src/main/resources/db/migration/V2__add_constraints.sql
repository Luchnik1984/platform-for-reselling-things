-- V2__add_constraints.sql
-- Идемпотентное добавление ограничений с IF NOT EXISTS

DO $$
BEGIN
    -- Внешний ключ ads -> users
    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints
                   WHERE constraint_name = 'fk_ads_author' AND table_name = 'ads') THEN
ALTER TABLE ads
    ADD CONSTRAINT fk_ads_author
        FOREIGN KEY (author_id)
            REFERENCES users(id)
            ON DELETE CASCADE;
END IF;
END $$;

DO $$
BEGIN
    -- Внешний ключ ads -> images
    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints
                   WHERE constraint_name = 'fk_ads_image' AND table_name = 'ads') THEN
ALTER TABLE ads
    ADD CONSTRAINT fk_ads_image
        FOREIGN KEY (image_id)
            REFERENCES images(id)
            ON DELETE SET NULL;
END IF;
END $$;

DO $$
BEGIN
    -- Внешний ключ comments -> users
    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints
                   WHERE constraint_name = 'fk_comments_author' AND table_name = 'comments') THEN
ALTER TABLE comments
    ADD CONSTRAINT fk_comments_author
        FOREIGN KEY (author_id)
            REFERENCES users(id)
            ON DELETE CASCADE;
END IF;
END $$;

DO $$
BEGIN
    -- Внешний ключ comments -> ads
    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints
                   WHERE constraint_name = 'fk_comments_ad' AND table_name = 'comments') THEN
ALTER TABLE comments
    ADD CONSTRAINT fk_comments_ad
        FOREIGN KEY (ad_id)
            REFERENCES ads(id)
            ON DELETE CASCADE;
END IF;
END $$;

DO $$
BEGIN
    -- Внешний ключ users -> images
    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints
                   WHERE constraint_name = 'fk_users_image' AND table_name = 'users') THEN
ALTER TABLE users
    ADD CONSTRAINT fk_users_image
        FOREIGN KEY (image_id)
            REFERENCES images(id)
            ON DELETE SET NULL;
END IF;
END $$;

DO $$
BEGIN
    -- Проверка роли
    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints
                   WHERE constraint_name = 'users_role_check' AND table_name = 'users') THEN
ALTER TABLE users
    ADD CONSTRAINT users_role_check
        CHECK (role IN ('USER', 'ADMIN'));
END IF;
END $$;

DO $$
BEGIN
    -- Проверка цены
    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints
                   WHERE constraint_name = 'ads_price_check' AND table_name = 'ads') THEN
ALTER TABLE ads
    ADD CONSTRAINT ads_price_check
        CHECK (price >= 0);
END IF;
END $$;