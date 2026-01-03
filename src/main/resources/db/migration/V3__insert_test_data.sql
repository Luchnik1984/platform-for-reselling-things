-- V3__insert_test_data.sql
-- Вставка тестовых данных

-- Очистка таблиц перед вставкой (на случай повторного запуска)
TRUNCATE TABLE comments, ads, users, images RESTART IDENTITY CASCADE;

-- Вставка пользователей
INSERT INTO users (email, password, first_name, last_name, phone, role)
VALUES
    ('admin@mail.ru', '$2a$12$hZFzf7tP56NGNj08U9gN1OdZ9hXwrrDAjIQDrLv.upeAxWaRnGmzS', 'Иван', 'Иванов', '+7 (999) 999-99-99', 'ADMIN'),
    ('user@mail.ru', '$2a$12$NS9M2mAVQj2tsrh4GWS1xeXKMAJWGHjhbJ09OVMya1sSvIhHQr2Aa', 'Петр', 'Петров', '+7 (988) 888-88-88', 'USER');

-- Вставка объявлений
INSERT INTO ads (title, price, description, author_id)
VALUES
    ('Продам велосипед', 15000, 'Горный велосипед', 1),
    ('Сниму квартиру', 35000, '2-комнатная в центре', 2);