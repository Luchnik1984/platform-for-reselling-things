package ru.skypro.homework.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Роли пользователей в системе")
public enum Role {
    @Schema(description = "Обычный пользователь")
    USER,

    @Schema(description = "Администратор системы")
    ADMIN
}
