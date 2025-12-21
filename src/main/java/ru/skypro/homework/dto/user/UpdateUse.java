package ru.skypro.homework.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


/**
 * DTO для обновления информации об авторизованном пользователе.
 * Содержит данные: имя, фамилия, телефон.
 */
@Data
public class UpdateUse {

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

}
