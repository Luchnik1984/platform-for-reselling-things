-- Создание таблиц

-- Таблица изображений (images)
CREATE TABLE images
(
    id         SERIAL PRIMARY KEY,
    file_path  VARCHAR(255) NOT NULL UNIQUE,
    file_size  BIGINT       NOT NULL CHECK (file_size > 0),
    media_type VARCHAR(50)  NOT NULL
);

-- Таблица пользователей (users)
CREATE TABLE users
(
    id         SERIAL PRIMARY KEY,
    email      VARCHAR(32) NOT NULL UNIQUE,
    password   VARCHAR(60) NOT NULL,
    first_name VARCHAR(16) NOT NULL,
    last_name  VARCHAR(16) NOT NULL,
    phone      VARCHAR(20) NOT NULL,
    role       VARCHAR(10) NOT NULL DEFAULT 'USER',
    enabled    BOOLEAN     NOT NULL DEFAULT TRUE,
    image_id   INT
);


-- Таблица объявлений (ads)
CREATE TABLE ads
(
    id          SERIAL PRIMARY KEY,
    title       VARCHAR(32) NOT NULL,
    price       INTEGER     NOT NULL CHECK (price >= 0),
    description VARCHAR(64) NOT NULL,
    author_id   INTEGER     NOT NULL,
    image_id    INTEGER,
    created_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Таблица комментариев (comments)
CREATE TABLE comments
(
    id         SERIAL PRIMARY KEY,
    text       VARCHAR(64) NOT NULL,
    author_id  INTEGER     NOT NULL,
    ad_id      INTEGER     NOT NULL,
    created_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);