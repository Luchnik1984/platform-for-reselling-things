package ru.skypro.homework.dto.comments;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Обёртка‑DTO для передачи списка комментариев.
 * Используется в REST‑методах, которые возвращают коллекцию комментариев
 * к объявлению (например, при открытии страницы объявления).
 * Содержит общее количество комментариев и список отдельных элементов
 * {@link Comment}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Comments {

    /**
     * Общее количество комментариев для объявления.
     */
    private Integer count;

    /**
     * Список комментариев.
     */
    private List<Comment> results;
}
