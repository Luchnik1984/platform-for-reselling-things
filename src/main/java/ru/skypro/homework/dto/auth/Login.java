package ru.skypro.homework.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * DTO для авторизации пользователя.
 * Содержит данные: логин пользователя, пароль пользователя.
 */
@Data
public class Login {

    @Schema(description = "Логин пользователя",
            example = "user@example.com",
            minLength = 4,
            maxLength = 32)
    private String username;

    @Schema(description = "Пароль пользователя",
            example = "password123",
            minLength = 8,
            maxLength = 16)
    private String password;
}
