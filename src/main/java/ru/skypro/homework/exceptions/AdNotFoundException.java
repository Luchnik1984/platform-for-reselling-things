package ru.skypro.homework.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение, выбрасываемое когда объявление не найдено.
 * Возвращает HTTP статус 404 (Not Found).
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class AdNotFoundException extends EntityException{

    /**
     * Создает исключение с ID объявления.
     *
     * @param id идентификатор объявления
     */
    public AdNotFoundException(Integer id) {
        super("Объявление с ID " + id + " не найдено");
    }
}
