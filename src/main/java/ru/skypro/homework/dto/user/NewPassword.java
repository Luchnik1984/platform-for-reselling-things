package ru.skypro.homework.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * DTO для обновления пароля пользователя.
 * Содержит данные: текущий пароль пользователя, новый пароль пользователя.
 */
@Data
public class NewPassword {

    @Schema(description = "Текущий пароль пользователя",
            example = "password123",
            minLength = 8,
            maxLength = 16)
    private String currentPassword;

    @Schema(description = "Новый пароль пользователя",
            example = "password123",
            minLength = 8,
            maxLength = 16)
    private String newPassword;
}
