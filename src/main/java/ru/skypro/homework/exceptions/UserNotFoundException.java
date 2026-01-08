package ru.skypro.homework.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение, выбрасываемое когда пользователь не найден.
 * Возвращает HTTP статус 404 (Not Found).
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class UserNotFoundException extends EntityException {

    /**
     * Создает исключение с ID пользователя.
     *
     * @param id идентификатор пользователя
     */

    public UserNotFoundException(Integer id) {
    super ("Пользователь с ID " + id + " не найден");
    }

    /**
     * Создает исключение с email пользователя.
     *
     * @param email email пользователя
     */
    public UserNotFoundException(String email) {
        super("Пользователь с email '" + email + "' не найден");
    }
}
