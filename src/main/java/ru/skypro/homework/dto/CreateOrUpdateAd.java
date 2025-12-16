package ru.skypro.homework.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

/**
 * DTO для получения данных от клиента при создании нового объявления (POST /ads)
 * или обновлении существующего (PATCH /ads/{id}).
 * Содержит только бизнес-информацию, которую предоставляет пользователь.
 * Технические поля (id, автор, ссылка на изображение) устанавливаются сервером.
 * Аннотации валидации ({@link Size}, {@link Min}, {@link Max}) гарантируют,
 * что входящие данные соответствуют бизнес-правилам, описанным в OpenAPI-спецификации.
 */
@Data
public class CreateOrUpdateAd {

    /**
     * Заголовок объявления.
     * Должен быть не менее 4 и не более 32 символов.
     */
    @Schema(
            description = "заголовок объявления",
            example = "Продам велосипед",
            minLength = 4,
            maxLength = 32
    )
    @Size(min = 4, max = 32, message = "Заголовок должен содержать от 4 до 32 символов")
    private String title;

    /**
     * Цена объявления в условных единицах.
     * Не может быть отрицательной или превышать установленный лимит.
     */
    @Schema(
            description = "цена объявления",
            example = "1500",
            minimum = "0",
            maximum = "10000000"
    )
    @Min(value = 0, message = "Цена не может быть отрицательной")
    @Max(value = 10_000_000, message = "Цена не может превышать 10 000 000")
    private Integer price;

    /**
     * Подробное текстовое описание товара.
     * Должно быть не менее 8 и не более 64 символов.
     */
    @Schema(
            description = "описание объявления",
            example = "Отличный горный велосипед. Год выпуска 2022.",
            minLength = 8,
            maxLength = 64
    )
    @Size(min = 8, max = 64, message = "Описание должно содержать от 8 до 64 символов")
    private String description;
}
