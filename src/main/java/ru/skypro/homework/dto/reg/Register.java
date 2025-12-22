package ru.skypro.homework.dto.reg;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import ru.skypro.homework.enums.Role;

@Data
public class Register {

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

    @Schema(description = "Имя пользователя",
            example = "Иван",
            minLength = 2,
            maxLength = 16)
    private String firstName;

    @Schema(description = "Фамилия пользователя",
            example = "Иванов",
            minLength = 2,
            maxLength = 16)
    private String lastName;

    @Schema(description = "Телефон пользователя",
            example = "+7 (999) 123-45-67",
            pattern = "\\+7\\s?\\(?\\d{3}\\)?\\s?\\d{3}-?\\d{2}-?\\d{2}")
    private String phone;

    @Schema(description = "Роль пользователя",
            example = "USER",
            allowableValues = {"USER", "ADMIN"})
    private Role role;
}
