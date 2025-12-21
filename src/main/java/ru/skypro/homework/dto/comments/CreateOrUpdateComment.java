package ru.skypro.homework.dto.comments;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
     */
    private String text;
}
