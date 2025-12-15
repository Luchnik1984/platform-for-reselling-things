package ru.skypro.homework.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

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
