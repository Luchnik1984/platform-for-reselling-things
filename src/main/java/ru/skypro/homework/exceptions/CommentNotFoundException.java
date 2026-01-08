package ru.skypro.homework.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение, выбрасываемое когда комментарий не найден.
 * Возвращает HTTP статус 404 (Not Found).
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class CommentNotFoundException extends EntityException {

    /**
     * Создает исключение с ID комментария.
     *
     * @param id идентификатор комментария
     */
    public CommentNotFoundException(Integer id) {
        super("Комментарий с ID " + id + " не найден");
    }
}
