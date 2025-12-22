package ru.skypro.homework.dto.comments;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO для передачи информации о комментарии к объявлению.
 * Используется при отображении списка комментариев и страницы объявления.
 * Содержит данные об авторе комментария, дате создания, тексте и
 * первичном ключе комментария.
 * Обратите внимание: DTO предназначен только для чтения на клиенте,
 * создание и изменение комментариев выполняется через {@link CreateOrUpdateComment}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Comment {

    /**
     * Отображаемое имя автора комментария.
     */
    private String author;

    /**
     * URL аватарки автора комментария.
     */
    @JsonProperty("authorImage")
    private String authorImage;

    /**
     * Имя автора комментария.
     */
    @JsonProperty("authorFirstName")
    private String authorFirstName;

    /**
     * Момент создания комментария (временная метка).
     */
    @JsonProperty("createdAt")
    private Instant createdAt;

    /**
     * Первичный ключ комментария.
     */
    private Integer pk;

    /**
     * Текст комментария.
     */
    private String text;
}
