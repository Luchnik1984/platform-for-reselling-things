-- V2__insert_test_data.sql
-- Вставка тестовых данных с ЗАШИФРОВАННЫМИ паролями

-- Пароли зашифрованы с помощью BCrypt (cost factor 10)
-- Формат: $2a$10$... (2a = алгоритм, 10 = cost factor)
-- Реальные пароли для тестов:
-- admin@example.com: admin123
-- user@example.com: password123
-- test@example.com: test123

INSERT INTO users (email, password, first_name, last_name, phone, role)
VALUES
-- Пароль: 'admin123'
('admin@mail.ru', '$2a$12$hZFzf7tP56NGNj08U9gN1OdZ9hXwrrDAjIQDrLv.upeAxWaRnGmzS', 'Иван', 'Иванов', '+7 (999) 999-99-99', 'ADMIN'),

-- Пароль: 'password123'
('user@mail.ru', '$2a$12$NS9M2mAVQj2tsrh4GWS1xeXKMAJWGHjhbJ09OVMya1sSvIhHQr2Aa', 'Петр', 'Петров', '+7 (988) 888-88-88', 'USER');