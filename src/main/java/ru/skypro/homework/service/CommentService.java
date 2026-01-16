package ru.skypro.homework.service;

import org.springframework.security.core.Authentication;
import ru.skypro.homework.dto.comments.Comment;
import ru.skypro.homework.dto.comments.Comments;
import ru.skypro.homework.dto.comments.CreateOrUpdateComment;
import ru.skypro.homework.exceptions.AccessDeniedException;
import ru.skypro.homework.exceptions.AdNotFoundException;
import ru.skypro.homework.exceptions.CommentNotFoundException;

/**
 * Сервис для управления комментариями к объявлениям.
 * Обеспечивает бизнес-логику работы с комментариями,
 * включая проверку прав доступа.
 *
 * <p>Права доступа:
 * <ul>
 *   <li>USER: может управлять только своими комментариями</li>
 *   <li>ADMIN: может управлять всеми комментариями</li>
 * </ul>
 *
 * <p>Особенности:
 * <ul>
 *   <li>Автоматически проставляет автора из данных аутентификации</li>
 *   <li>Проверяет существование объявления перед добавлением комментария</li>
 *   <li>Обеспечивает каскадное удаление при удалении объявления</li>
 * </ul>
 *
 * @see ru.skypro.homework.controller.CommentController
 */
public interface CommentService {

    /**
     * Получает все комментарии указанного объявления.
     * Доступно всем аутентифицированным пользователям.
     *
     * @param adId идентификатор объявления
     * @return список комментариев с общим количеством
     * @throws AdNotFoundException если объявление не найдено
     */
    Comments getComments(Integer adId);

    /**
     * Добавляет новый комментарий к объявлению.
     * Автор комментария определяется из данных аутентификации.
     *
     * @param adId идентификатор объявления
     * @param authentication объект аутентификации Spring Security
     * @param dto DTO с текстом комментария
     * @return созданный комментарий
     * @throws AdNotFoundException если объявление не найдено
     */
    Comment addComment(Integer adId, Authentication authentication, CreateOrUpdateComment dto);

    /**
     * Удаляет комментарий.
     * Проверяет права доступа: только автор или ADMIN.
     *
     * @param adId идентификатор объявления
     * @param commentId идентификатор комментария
     * @param authentication объект аутентификации
     * @throws CommentNotFoundException если комментарий не найден
     * @throws AccessDeniedException если пользователь не имеет прав
     */
    void deleteComment(Integer adId, Integer commentId, Authentication authentication);

    /**
     * Обновляет текст комментария.
     * Проверяет права доступа: только автор или ADMIN.
     *
     * @param adId идентификатор объявления
     * @param commentId идентификатор комментария
     * @param authentication объект аутентификации
     * @param dto DTO с обновлённым текстом
     * @return обновлённый комментарий
     * @throws CommentNotFoundException если комментарий не найден
     * @throws AccessDeniedException если пользователь не имеет прав
     */
    Comment updateComment(Integer adId, Integer commentId, Authentication authentication, CreateOrUpdateComment dto);
}
