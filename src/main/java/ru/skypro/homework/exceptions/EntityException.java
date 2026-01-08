package ru.skypro.homework.exceptions;

/**
 * Базовое исключение для всех сущностей.
 * Все кастомные исключения сущностей наследуются от этого класса.
 */
public class EntityException extends RuntimeException {

    /**
     * Создает новое исключение с сообщением.
     *
     * @param message описание ошибки
     */
    public EntityException(String message) {
        super(message);
    }

    /**
     * Создает новое исключение с сообщением и причиной.
     *
     * @param message описание ошибки
     * @param cause первоначальная причина исключения
     */
    public EntityException(String message, Throwable cause) {
        super(message, cause);
    }
}
