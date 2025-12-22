package ru.skypro.homework.dto.comments;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


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
     * ID автора комментария.
     * В OpenAPI: type: integer, format: int32
     * Это не имя, а именно числовой идентификатор!
     */

    @Schema(
            description = "id автора комментария",
            example = "1",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private Integer author;

    /**
     * Ссылка на аватар автора.
     */
    @Schema(
            description = "ссылка на аватар автора комментария",
            example = "/images/users/1-avatar.jpg",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private String authorImage;

    /**
     * Имя автора для отображения.
     */
    @Schema(
            description = "имя создателя комментария",
            example = "Иван",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private String authorFirstName;

    /**
     * Время создания в миллисекундах.
     * В OpenAPI: type: integer, format: int64
     * Миллисекунды - большое число (Long)
     */
    @Schema(description = "дата и время создания комментария в миллисекундах",
            example = "1644355200000")
    private Long createdAt;

    /**
     * ID комментария.
     */
    @Schema(
            description = "id комментария",
            example = "1",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Integer pk;

    @Schema(
            description = "текст комментария",
            example = "Отличный товар, рекомендую!",
            accessMode = Schema.AccessMode.READ_ONLY)
    private String text;
}
