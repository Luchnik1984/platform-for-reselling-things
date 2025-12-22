package ru.skypro.homework.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.skypro.homework.dto.comments.Comment;
import ru.skypro.homework.dto.comments.Comments;
import ru.skypro.homework.dto.comments.CreateOrUpdateComment;

/**
 * REST‑контроллер для работы с комментариями к объявлениям.
 * Отвечает за получение списка комментариев, добавление новых,
 * обновление и удаление существующих комментариев.
 * Данный контроллер реализует минимальный набор эндпоинтов
 * в виде заглушек и возвращает пустые DTO‑объекты.
 * Полная бизнес‑логика будет добавлена на следующих этапах разработки.
 */
@RestController
@RequestMapping("/ads")
@RequiredArgsConstructor
public class CommentController {

    /**
     * Получение списка комментариев для указанного объявления.
     * На данном этапе реализована заглушка: возвращается пустой объект {@link Comments}
     * с нулевым количеством комментариев и пустым списком результатов.
     *
     * @param id идентификатор объявления
     * @return пустой {@link Comments}
     */
    @GetMapping("/{id}/comments")
    public ResponseEntity<Comments> getComments(@PathVariable("id") Integer id) {
        Comments empty = new Comments(0, java.util.List.of());
        return ResponseEntity.ok(empty);
    }

    /**
     * Добавление нового комментария к объявлению.
     * На данном этапе реализована заглушка: игнорирует тело запроса
     * и возвращает пустой объект {@link Comment} без заполненных полей.
     *
     * @param id   идентификатор объявления
     * @param dto  DTO с текстом комментария
     * @return пустой {@link Comment}
     */
    @PostMapping("/{id}/comments")
    public ResponseEntity<Comment> addComment(@PathVariable("id") Integer id,
                                              @RequestBody CreateOrUpdateComment dto) {
        Comment empty = new Comment();
        return ResponseEntity.ok(empty);
    }

    /**
     * Удаление комментария по идентификатору для указанного объявления.
     * На данном этапе реализована заглушка: метод не выполняет
     * фактического удаления и просто возвращает успешный статус 200 OK.
     *
     * @param adId       идентификатор объявления
     * @param commentId  идентификатор комментария
     * @return пустой ответ со статусом 200 OK
     */
    @DeleteMapping("/{adId}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable("adId") Integer adId,
                                              @PathVariable("commentId") Integer commentId) {
        return ResponseEntity.ok().build();
    }

    /**
     * Частичное обновление комментария (изменение текста) для указанного объявления.
     * На данном этапе реализована заглушка: игнорирует новый текст комментария
     * и возвращает пустой объект {@link Comment} без заполненных полей.
     *
     * @param adId       идентификатор объявления
     * @param commentId  идентификатор комментария
     * @param dto        DTO с обновлённым текстом комментария
     * @return пустой {@link Comment}
     */
    @PatchMapping("/{adId}/comments/{commentId}")
    public ResponseEntity<Comment> updateComment(@PathVariable("adId") Integer adId,
                                                 @PathVariable("commentId") Integer commentId,
                                                 @RequestBody CreateOrUpdateComment dto) {
        Comment empty = new Comment();
        return ResponseEntity.ok(empty);
    }
}