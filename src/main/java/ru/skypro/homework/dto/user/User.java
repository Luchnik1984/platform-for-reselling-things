package ru.skypro.homework.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import ru.skypro.homework.enums.Role;


/**
 * DTO для получения данных о пользователе.
 * Используется в ответе GET /users/me.
 * Содержит данные: ID, логин, имя, фамилия, телефон, роль, ссылка на аватар пользователя.
 */
@Data
public class User {

    @Schema(description = "ID пользователя",
            example = "1",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private Integer id;

    /**
     * Email (логин) пользователя.
     * В OpenAPI: username в Register, но email в User
     * Это нормально - на входе username, в ответе email
     */
    @Schema(
            description = "Логин пользователя",
            example = "user@example.com",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private String email;

    @Schema(
            description = "Имя пользователя",
            example = "Иван",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private String firstName;

    @Schema(
            description = "Фамилия пользователя",
            example = "Иванов",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private String lastName;

    @Schema(
            description = "Телефон пользователя",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private String phone;

    @Schema(
            description = "Роль пользователя",
            example = "USER",
            allowableValues = {"USER", "ADMIN"},
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private Role role;

    @Schema(description = "Ссылка на аватар пользователя",
            example = "https://foni.papik.pro/uploads/posts/2024-09/thumbs/foni-papik-pro-if4w-p-kartinki-spanch-bob-na-prozrachnom-fone-1.png",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private String image;
}
