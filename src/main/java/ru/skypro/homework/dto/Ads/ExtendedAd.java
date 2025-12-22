package ru.skypro.homework.dto.Ads;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import ru.skypro.homework.dto.reg.Register;

/**
 * DTO для передачи полной, детализированной информации об объявлении.
 * Используется при открытии страницы объявления.
 * Содержит все данные из {@link Ad}, а также расширенное описание
 * и контактную информацию об авторе (имя, фамилия, email, телефон).
 * Обратите внимание: в отличие от {@link Ad}, данный DTO не содержит
 * поля {@code author} (id автора), но включает поля {@code authorFirstName}
 * и {@code authorLastName}.
 */
@Data
public class ExtendedAd {

    /**
     * Уникальный идентификатор объявления.
     */
    @Schema(
            description = "id объявления",
            example = "123",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private Integer pk;

    /**
     * Имя автора объявления.
     */
    @Schema(
            description = "имя автора объявления",
            example = "Иван"
    )
    private String authorFirstName;

    /**
     * Фамилия автора объявления.
     */
    @Schema(
            description = "фамилия автора объявления",
            example = "Иванов"
    )
    private String authorLastName;

    /**
     * Подробное текстовое описание товара или услуги.
     */
    @Schema(
            description = "описание объявления",
            example = "Отличный горный велосипед в идеальном состоянии. Год выпуска 2022."
    )
    private String description;

    /**
     * Электронная почта (логин) автора для связи.
     * Может дублировать username из {@link Register}.
     */
    @Schema(
            description = "логин автора объявления",
            example = "user@example.com"
    )
    private String email;

    /**
     * Ссылка на главное изображение объявления.
     */
    @Schema(
            description = "ссылка на картинку объявления",
            example = "/images/ads/123-image.jpg"
    )
    private String image;

    /**
     * Контактный телефон автора.
     * Формат соответствует полю phone в {@link Register}.
     */
    @Schema(
            description = "телефон автора объявления",
            example = "+7 (999) 123-45-67"
    )
    private String phone;

    /**
     * Цена товара или услуги.
     */
    @Schema(
            description = "цена объявления",
            example = "1500"
    )
    private Integer price;

    /**
     * Краткий заголовок объявления.
     */
    @Schema(
            description = "заголовок объявления",
            example = "Продам горный велосипед"
    )
    private String title;
}
