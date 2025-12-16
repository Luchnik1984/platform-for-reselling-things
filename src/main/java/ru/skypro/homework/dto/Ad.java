package ru.skypro.homework.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * DTO для передачи краткой информации об объявлении.
 * Используется для отображения объявления в списках (например, на главной странице).
 * Содержит базовые данные: идентификатор, заголовок, цену, ссылку на изображение и автора.
 */
@Data
public class Ad {

    /**
     * Уникальный идентификатор объявления.
     * Соответствует полю "pk" в спецификации OpenAPI.
     */
    @Schema(
            description = "id объявления",
            example = "123",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private Integer pk;

    /**
     * Идентификатор автора (пользователя), создавшего объявление.
     * Соответствует полю "author" в спецификации OpenAPI.
     */
    @Schema(
            description = "id автора объявления",
            example = "1",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private Integer author;

    /**
     * Ссылка на изображение объявления.
     * Может быть строкой-заглушкой (например, "/images/default.jpg").
     */
    @Schema(
            description = "ссылка на картинку объявления",
            example = "/images/ads/123-image.jpg"
    )
    private String image;

    /**
     * Цена объявления в условных единицах.
     */
    @Schema(
            description = "цена объявления",
            example = "1500"
    )
    private Integer price;

    /**
     * Заголовок объявления.
     */
    @Schema(
            description = "заголовок объявления",
            example = "Продам велосипед"
    )
    private String title;

}