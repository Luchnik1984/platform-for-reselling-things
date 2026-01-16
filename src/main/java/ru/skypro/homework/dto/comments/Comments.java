package ru.skypro.homework.dto.comments;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Обёртка‑DTO для передачи списка комментариев.
 * Используется в ответе GET /ads/{id}/comments.
 * Содержит общее количество комментариев и список отдельных элементов
 * {@link Comment}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Comments {


    @Schema(
            description = "общее количество комментариев",
            example = "5"
    )
    private Integer count;


    @Schema(
            description = "список комментариев"
    )
    private List<Comment> results;
}
