package ru.skypro.homework.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.skypro.homework.dto.comments.Comment;
import ru.skypro.homework.dto.comments.Comments;
import ru.skypro.homework.dto.comments.CreateOrUpdateComment;
import ru.skypro.homework.repository.UserRepository;
import ru.skypro.homework.service.CommentService;

import javax.validation.Valid;
import java.util.List;

/**
 * REST‑контроллер для работы с комментариями к объявлениям.
 * Отвечает за получение списка комментариев, добавление новых,
 * обновление и удаление существующих комментариев.
 * Данный контроллер реализует минимальный набор эндпоинтов
 * в виде заглушек и возвращает пустые DTO‑объекты.
 * На начальном этапе реализованы заглушки всех методов.
 * Полная бизнес‑логика будет добавлена на следующих этапах разработки.
 */
@Slf4j
@CrossOrigin(value = "http://localhost:3000")
@RestController
@RequiredArgsConstructor
@Tag(name = "Комментарии", description = "API для работы с комментариями к объявлениям")
@RequestMapping("/ads")

public class CommentController {

    private final CommentService commentService;
    private final UserRepository userRepository;


    /**
     * Получение списка комментариев для указанного объявления.
     * На данном этапе реализована заглушка: возвращается пустой объект {@link Comments}
     * с нулевым количеством комментариев и пустым списком результатов.
     *
     * @param id идентификатор объявления
     * @return пустой {@link Comments}
     */
    @Operation(summary = "Получение комментариев объявления")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OK",
                    content = @Content(schema = @Schema(implementation = Comments.class))
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @GetMapping("/{id}/comments")
    @ResponseStatus(HttpStatus.OK)
    public Comments getComments(@PathVariable("id") Integer id) {
        log.info("Получение комментариев для объявления ID={}", id);

        return new Comments(0, List.of()); // Заглушка: возвращаем пустой список
    }

    /**
     * Добавление нового комментария к объявлению.
     * На данном этапе реализована заглушка: игнорирует тело запроса
     * и возвращает пустой объект {@link Comment} без заполненных полей.
     *
     * @param id  идентификатор объявления
     * @param dto DTO с текстом комментария
     * @return пустой {@link Comment}
     */
    @Operation(summary = "Добавление комментария к объявлению")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OK",
                    content = @Content(schema = @Schema(implementation = Comment.class))
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @PostMapping("/{id}/comments")
    @ResponseStatus(HttpStatus.OK)
    public Comment addComment(@PathVariable("id") Integer id,
                              @Valid @RequestBody CreateOrUpdateComment dto) {
        log.info("Добавление комментария к объявлению ID={}", id);
        // Заглушка: создаем фиктивный комментарий
        Comment comment = new Comment();
        comment.setAuthor(1);
        comment.setAuthorImage("/images/users/1-avatar.jpg");
        comment.setAuthorFirstName("Иван");
        comment.setCreatedAt(System.currentTimeMillis());
        comment.setPk(1);
        comment.setText(dto.getText() != null ? dto.getText() : "Тестовый комментарий");
        return comment;
    }

    /**
     * Удаление комментария по идентификатору для указанного объявления.
     * На данном этапе реализована заглушка: метод не выполняет фактического удаления.
     *
     * @param adId      идентификатор объявления
     * @param commentId идентификатор комментария
     *                  Логируем удаление - заглушка
     */
    @Operation(summary = "Удаление комментария")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @DeleteMapping("/{adId}/comments/{commentId}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteComment(@PathVariable("adId") Integer adId,
                              @PathVariable("commentId") Integer commentId) {
        log.info("Удаление комментария ID={} из объявления ID={}", commentId, adId);
    }

    /**
     * Частичное обновление комментария (изменение текста) для указанного объявления.
     * На данном этапе реализована заглушка: игнорирует новый текст комментария
     * и возвращает пустой объект {@link Comment} без заполненных полей.
     *
     * @param adId      идентификатор объявления
     * @param commentId идентификатор комментария
     * @param dto       DTO с обновлённым текстом комментария
     * @return пустой {@link Comment}
     */
    @Operation(summary = "Обновление комментария")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OK",
                    content = @Content(schema = @Schema(implementation = Comment.class))
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @PatchMapping("/{adId}/comments/{commentId}")
    @ResponseStatus(HttpStatus.OK)
    public Comment updateComment(@PathVariable("adId") Integer adId,
                                 @PathVariable("commentId") Integer commentId,
                                 @Valid @RequestBody CreateOrUpdateComment dto) {
        log.info("Обновление комментария ID={} в объявлении ID={}", commentId, adId);
        // Заглушка: создаем "обновленный" комментарий
        Comment comment = new Comment();
        comment.setAuthor(1);
        comment.setAuthorImage("/images/users/1-avatar.jpg");
        comment.setAuthorFirstName("Иван");
        comment.setCreatedAt(System.currentTimeMillis());
        comment.setPk(commentId);
        comment.setText(dto.getText() != null ? dto.getText() : "Обновленный комментарий");
        return comment;
    }
}