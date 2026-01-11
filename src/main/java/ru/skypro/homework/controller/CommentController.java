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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.skypro.homework.dto.comments.Comment;
import ru.skypro.homework.dto.comments.Comments;
import ru.skypro.homework.dto.comments.CreateOrUpdateComment;
import ru.skypro.homework.service.CommentService;

import javax.validation.Valid;

/**
 * REST‑контроллер для работы с комментариями к объявлениям.
 * Отвечает за получение списка комментариев, добавление новых,
 * обновление и удаление существующих комментариев.
 *
 * <p>Архитектура: Контроллер делегирует бизнес-логику {@link CommentService}.
 * Выполняет:
 * <ul>
 *   <li>Валидацию входящих данных (аннотации {@code @Valid})</li>
 *   <li>Извлечение параметров из запроса ({@code @PathVariable})</li>
 *   <li>Формирование HTTP-ответов с правильными статусами</li>
 *   <li>Передачу контекста аутентификации в сервисный слой</li>
 * </ul>
 *
 * <p>Проверка прав доступа:
 * <ul>
 *   <li>USER: может управлять только своими комментариями</li>
 *   <li>ADMIN: может управлять всеми комментариями</li>
 *   <li>Проверка выполняется на уровне сервиса ({@link CommentService})</li>
 * </ul>
 *
 * @see CommentService сервис, реализующий бизнес-логику операций с комментариями
 * @see Comment DTO для отображения информации о комментарии
 * @see Comments DTO-обёртка для списка комментариев
 * @see CreateOrUpdateComment DTO для получения данных от клиента при создании/обновлении
 */
@Slf4j
@CrossOrigin(value = "http://localhost:3000")
@RestController
@RequiredArgsConstructor
@Tag(name = "Комментарии", description = "API для работы с комментариями к объявлениям")
@RequestMapping("/ads")

public class CommentController {

    private final CommentService commentService;

    /**
     * Получение списка комментариев для указанного объявления.
     * <p>Эндпоинт доступен для всех аутентифицированных пользователей.
     * Возвращает все комментарии, относящиеся к указанному объявлению,
     * включая информацию об авторах комментариев.
     *
     * <p>Особенности реализации:
     * <ul>
     *   <li>Использует {@link CommentService#getComments(Integer)} для получения данных</li>
     *   <li>Возвращает обёртку {@link Comments} с общим количеством и списком</li>
     *   <li>Автоматически преобразует исключения в HTTP статусы</li>
     * </ul>
     *
     * @return {@link Comments} со списком комментариев объявления
     */
    @Operation(
            summary = "Получение комментариев объявления",
            description = "Возвращает список всех комментариев, относящихся к указанному объявлению. " +
                    "Включает информацию об авторах комментариев (имя, аватар). " +
                    "Требуется аутентификация."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OK",
                    content = @Content(schema = @Schema(implementation = Comments.class))
            ),
            @ApiResponse(responseCode = "401",
                    description = "Unauthorized - требуется аутентификация",
                    content = @Content(schema = @Schema(hidden = true))
            ),
            @ApiResponse(responseCode = "404",
                    description = "Not found - объявление не найдено",
                    content = @Content(schema = @Schema(hidden = true))
            )
    })
    @GetMapping("/{id}/comments")
    @ResponseStatus(HttpStatus.OK)
    public Comments getComments(@PathVariable("id") Integer id) {
        log.info("Получение комментариев для объявления ID={}", id);

        Comments comments = commentService.getComments(id);

        log.debug("Найдено {} комментариев для объявления ID={}", comments.getCount(), id);

        return comments;
    }

    /**
     * Добавление нового комментария к объявлению.
     * <p>Эндпоинт доступен для всех аутентифицированных пользователей.
     * Автор комментария определяется автоматически из данных аутентификации.
     * Время создания комментария устанавливается сервером.
     *
     * <p>Особенности реализации:
     * <ul>
     *   <li>Использует {@link CommentService#addComment(Integer, Authentication, CreateOrUpdateComment)}</li>
     *   <li>Автор определяется автоматически из {@link Authentication}</li>
     *   <li>Валидирует входящие данные через {@code @Valid}</li>
     *   <li>Возвращает созданный комментарий с заполненными серверными полями</li>
     * </ul>
     *
     * @param id идентификатор объявления, к которому добавляется комментарий
     * @param dto DTO {@link CreateOrUpdateComment}, содержащий текст комментария
     * @param authentication объект аутентификации текущего пользователя
     * @return созданный комментарий в формате {@link Comment}
     */
    @Operation(
            summary = "Добавление комментария к объявлению",
            description = "Создаёт новый комментарий к указанному объявлению. " +
                    "Автор комментария определяется автоматически из данных аутентификации. " +
                    "Время создания устанавливается сервером. " +
                    "Требуется аутентификация."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OK - комментарий успешно создан",
                    content = @Content(schema = @Schema(implementation = Comment.class))
            ),
            @ApiResponse(responseCode = "401",
                    description = "Unauthorized - требуется аутентификация",
                    content = @Content(schema = @Schema(hidden = true))
            ),
            @ApiResponse(responseCode = "404",
                    description = "Not found - объявление не найдено",
                    content = @Content(schema = @Schema(hidden = true))
            )
    })
    @PostMapping("/{id}/comments")
    @ResponseStatus(HttpStatus.OK)
    public Comment addComment(@PathVariable("id") Integer id,
                              @Valid @RequestBody CreateOrUpdateComment dto,
                              Authentication authentication) {
        log.info("Добавление комментария к объявлению ID={} пользователем: {}",
                id, authentication.getName());
        log.debug("Текст комментария: {}", dto.getText());

        Comment createdComment = commentService.addComment(id, authentication, dto);

        log.info("Комментарий ID={} успешно создан для объявления ID={}",
                createdComment.getPk(), id);
        return createdComment;

    }

    /**
     * Удаление комментария по идентификатору для указанного объявления.
     * <p>Проверка прав доступа:
     * <ul>
     *   <li>USER может удалять только свои комментарии</li>
     *   <li>ADMIN может удалять любые комментарии</li>
     *   <li>При попытке удалить чужой комментарий возвращается 403 Forbidden</li>
     * </ul>
     *
     * <p>Особенности реализации:
     * <ul>
     *   <li>Использует {@link CommentService#deleteComment(Integer, Integer, Authentication)}</li>
     *   <li>Проверка прав доступа выполняется на уровне сервиса</li>
     *   <li>Возвращает статус 200 OK при успешном удалении</li>
     * </ul>
     *
     * @param adId идентификатор объявления
     * @param commentId идентификатор удаляемого комментария
     * @param authentication объект аутентификации текущего пользователя
     */
    @Operation(
            summary = "Удаление комментария",
            description = "Удаляет комментарий по идентификатору. " +
                    "USER может удалять только свои комментарии. " +
                    "ADMIN может удалять любые комментарии. " +
                    "Требуется аутентификация."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"
                    , description = "OK  - комментарий успешно удалён"
            ),
            @ApiResponse(responseCode = "401",
                    description = "Unauthorized - требуется аутентификация",
                    content = @Content(schema = @Schema(hidden = true))
            ),
            @ApiResponse(responseCode = "403",
                    description = "Forbidden - недостаточно прав для удаления",
                    content = @Content(schema = @Schema(hidden = true))
            ),
            @ApiResponse(responseCode = "404",
                    description = "Not found - комментарий или объявление не найдены",
                    content = @Content(schema = @Schema(hidden = true))
            )
    })
    @DeleteMapping("/{adId}/comments/{commentId}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteComment(@PathVariable("adId") Integer adId,
                              @PathVariable("commentId") Integer commentId,
                              Authentication authentication) {
        log.info("Удаление комментария ID={} из объявления ID={} пользователем: {}",
                commentId, adId, authentication.getName());

        commentService.deleteComment(adId, commentId, authentication);

        log.info("Комментарий ID={} успешно удалён", commentId);
    }

    /**
     * Частичное обновление комментария (изменение текста) для указанного объявления.
     * <p>Проверка прав доступа:
     * <ul>
     *   <li>USER может обновлять только свои комментарии</li>
     *   <li>ADMIN может обновлять любые комментарии</li>
     *   <li>При попытке обновить чужой комментарий возвращается 403 Forbidden</li>
     * </ul>
     *
     * <p>Особенности реализации:
     * <ul>
     *   <li>Использует {@link CommentService#updateComment(Integer, Integer,
     *   Authentication, CreateOrUpdateComment)}</li>
     *   <li>Проверка прав доступа выполняется на уровне сервиса</li>
     *   <li>Обновляет только текст комментария, остальные поля остаются неизменными</li>
     *   <li>Возвращает обновлённый комментарий</li>
     * </ul>
     *
     * @param adId      идентификатор объявления
     * @param commentId идентификатор комментария
     * @param dto       DTO с обновлённым текстом комментария
     * @param authentication объект аутентификации текущего пользователя
     * @return обновлённый комментарий в формате {@link Comment}
     */
    @Operation(
            summary = "Обновление комментария",
            description = "Обновляет текст существующего комментария. " +
                    "USER может обновлять только свои комментарии. " +
                    "ADMIN может обновлять любые комментарии. " +
                    "Требуется аутентификация."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OK - комментарий успешно обновлён",
                    content = @Content(schema = @Schema(implementation = Comment.class))
            ),
            @ApiResponse(responseCode = "401",
                    description = "Unauthorized  - требуется аутентификация ",
                    content = @Content(schema = @Schema(hidden = true))
            ),
            @ApiResponse(responseCode = "403",
                    description = "Forbidden - недостаточно прав для обновления",
                    content = @Content(schema = @Schema(hidden = true))
            ),
            @ApiResponse(responseCode = "404",
                    description = "Not found - комментарий или объявление не найдены",
                    content = @Content(schema = @Schema(hidden = true))
            )
    })
    @PatchMapping("/{adId}/comments/{commentId}")
    @ResponseStatus(HttpStatus.OK)
    public Comment updateComment(@PathVariable("adId") Integer adId,
                                 @PathVariable("commentId") Integer commentId,
                                 @Valid @RequestBody CreateOrUpdateComment dto,
                                 Authentication authentication) {
        log.info("Обновление комментария ID={} в объявлении ID={} пользователем: {}",
                commentId, adId, authentication.getName());

        Comment updatedComment = commentService.updateComment(adId, commentId, authentication, dto);

        log.info("Комментарий ID={} успешно обновлён", commentId);
        return updatedComment;
    }
}