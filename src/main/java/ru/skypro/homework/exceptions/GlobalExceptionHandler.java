package ru.skypro.homework.exceptions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Глобальный обработчик исключений для всего приложения.
 * Преобразует Java исключения в HTTP-ответы согласно OpenAPI спецификации.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * DTO для ответа об ошибке.
     * Используется для структурированного возврата ошибок клиенту.
     */
    @Data
    @AllArgsConstructor
    public static class ValidationErrorResponse {
        /** Сообщение об ошибке для пользователя */
        private String message;

        /** Время возникновения ошибки */
        private LocalDateTime timestamp;

        /** Детали ошибки (например, ошибки валидации по полям) */
        private Map<String, String> details;
    }

    /**
     * Обрабатывает ошибки валидации (@Valid в контроллерах).
     * Используется для POST /register и других эндпоинтов с валидацией.
     *
     * @param ex исключение валидации
     * @return 400 Bad Request с деталями ошибок
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));

        ValidationErrorResponse error = new ValidationErrorResponse(
                "Ошибка валидации входных данных",
                LocalDateTime.now(),
                errors
        );

        log.warn("Ошибка валидации: {}", errors);
        return ResponseEntity.badRequest().body(error);
    }


    /**
     * Обрабатывает неверные учетные данные (логин/пароль).
     * Используется для POST /login.
     *
     * @param ex исключение неверных учетных данных
     * @return 401 Unauthorized
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Void> handleBadCredentials(BadCredentialsException ex) {
        log.warn("Ошибка аутентификации: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    /**
     * Обрабатывает недостаток прав доступа.
     * Используется при попытке изменить/удалить чужой ресурс.
     *
     * @param ex исключение доступа
     * @return 403 Forbidden
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Void> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Ошибка доступа: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    /**
     * Обрабатывает неверный пароль при смене пароля.
     * Используется для POST /users/set_password.
     *
     * @param ex исключение неверного пароля
     * @return 401 Unauthorized
     */
    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<Void> handleInvalidPassword(InvalidPasswordException ex) {
        log.warn("Неверный пароль: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    /**
     * Обрабатывает некорректные аргументы методов.
     *
     * @param ex исключение неверного аргумента
     * @return 400 Bad Request
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Void> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Некорректные аргументы: {}", ex.getMessage());
        return ResponseEntity.badRequest().build();
    }

    /**
     * Обрабатывает ситуации, когда сущность не найдена.
     * Включает AdNotFoundException, CommentNotFoundException, UserNotFoundException.
     *
     * @param ex исключение "не найдено"
     * @return 404 Not Found
     */
    @ExceptionHandler(EntityException.class)
    public ResponseEntity<Void> handleEntityNotFoundException(EntityException ex) {
        log.warn("Ресурс не найден: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    /**
     * Обрабатывает все остальные исключения.
     * Возвращает 500 Internal Server Error.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Void> handleGenericException(Exception ex) {
        log.error("Необработанное исключение: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}
