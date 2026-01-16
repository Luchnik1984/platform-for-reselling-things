package ru.skypro.homework.dto.comments;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;

/**
 * DTO для создания или обновления комментария к объявлению.
 * Используется в теле запросов REST‑методов добавления и редактирования
 * комментариев.
 * Содержит только текст комментария, остальные поля (автор, дата создания,
 * идентификатор) заполняются на сервере и возвращаются в {@link Comment}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrUpdateComment {

    /**
     * Текст комментария.
     * Обязательное поле, от 8 до 64 символов.
     */
    @Schema(
            description = "текст комментария",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minLength = 8,
            maxLength = 64,
            example = "Этот товар в отличном состоянии!"
    )
    @Size(min = 8, max = 64, message = "Текст комментария должен содержать от 8 до 64 символов")
    private String text;
}
