package ru.skypro.homework.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение, выбрасываемое когда у пользователя нет прав на операцию.
 * Возвращает HTTP статус 403 (Forbidden).
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class AccessDeniedException extends RuntimeException {

    /**
     * Создает исключение с кастомным сообщением.
     *
     * @param message описание ошибки
     */
    public AccessDeniedException(String message) {
        super(message);
    }

    /**
     * Создает исключение для конкретного ресурса.
     *
     * @param resourceType тип ресурса
     * @param resourceId идентификатор ресурса
     */
    public AccessDeniedException(String resourceType, Integer resourceId) {
        super("Нет доступа к " + resourceType + " с ID " + resourceId);
    }
}
