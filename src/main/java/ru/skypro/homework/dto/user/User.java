package ru.skypro.homework.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import ru.skypro.homework.enums.Role;

import java.util.UUID;


/**
 * DTO для получения данных о пользователе.
 * Содержит данные: ID, логин, имя, фамилия, телефон, роль, ссылка на аватар пользователя.
 */
@Data
public class User {

    @Schema(description = "ID пользователя",
            example = "user@example.com",
            minLength = 4,
            maxLength = 32)
    private UUID id;

    @Schema(description = "Логин пользователя",
            example = "user@example.com",
            minLength = 4,
            maxLength = 32)
    private String email;

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

    @Schema(description = "Ссылка на аватар пользователя",
            example = "https://foni.papik.pro/uploads/posts/2024-09/thumbs/foni-papik-pro-if4w-p-kartinki-spanch-bob-na-prozrachnom-fone-1.png",
            pattern = "(?:https?://)?(?:[a-z0-9\\-]+\\.)+[a-z]{2,}(?:/[\\w\\-./?%&=]*)?\\.(?:jpg|jpeg|png|gif|bmp|webp|svg)(?:\\?.*)?")
    private String image;
}
