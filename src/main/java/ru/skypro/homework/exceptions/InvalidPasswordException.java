package ru.skypro.homework.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение, выбрасываемое когда предоставлен неверный пароль.
 * Возвращает HTTP статус 400 (Bad Request).
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidPasswordException extends RuntimeException {

    /**
     * Создает исключение с кастомным сообщением.
     *
     * @param message описание ошибки
     */
    public InvalidPasswordException(String message) {
        super(message);
    }

    /**
     * Создает исключение со стандартным сообщением.
     */
    public InvalidPasswordException() {
        super("Неверный пароль");
    }
}
