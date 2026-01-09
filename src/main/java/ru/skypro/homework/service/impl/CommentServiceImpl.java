package ru.skypro.homework.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import ru.skypro.homework.dto.comments.Comment;
import ru.skypro.homework.dto.comments.Comments;
import ru.skypro.homework.dto.comments.CreateOrUpdateComment;
import ru.skypro.homework.entity.AdEntity;
import ru.skypro.homework.entity.CommentEntity;
import ru.skypro.homework.entity.UserEntity;
import ru.skypro.homework.exceptions.AdNotFoundException;
import ru.skypro.homework.exceptions.CommentNotFoundException;
import ru.skypro.homework.mapper.CommentMapper;
import ru.skypro.homework.repository.AdRepository;
import ru.skypro.homework.repository.CommentRepository;
import ru.skypro.homework.repository.UserRepository;
import ru.skypro.homework.service.CommentService;
import ru.skypro.homework.util.SecurityUtils;

import java.util.List;

/**
 * Реализация сервиса для управления комментариями.
 * Обеспечивает бизнес-логику работы с комментариями,
 * включая проверку прав доступа авторов.
 *
 * <p>Использует:
 * <ul>
 *   <li>{@link CommentRepository} - доступ к данным комментариев</li>
 *   <li>{@link AdRepository} - проверка существования объявлений</li>
 *   <li>{@link CommentMapper} - преобразование Entity <-> DTO</li>
 *   <li>{@link SecurityUtils} - получение аутентифицированного пользователя</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final AdRepository adRepository;
    private final CommentMapper commentMapper;
    private final UserRepository userRepository;

    /**
     * {@inheritDoc}
     *
     * <p>Реализация:
     * <ol>
     *   <li>Проверяет существование объявления</li>
     *   <li>Получает все комментарии объявления из репозитория</li>
     *   <li>Преобразует в DTO и возвращает обёртку</li>
     * </ol>
     */
    @Override
    @Transactional(readOnly = true)
    public Comments getComments(Integer adId) {
        log.debug("Получение комментариев для объявления ID: {}", adId);

        if (!adRepository.existsById(adId)) {
            throw new AdNotFoundException(adId);
        }

        List<Comment> results = commentRepository.findAllByAdId(adId)
                .stream()
                .map(commentMapper::toDto)
                .toList();

        Comments comments = new Comments(results.size(), results);

        log.trace("Найдено {} комментариев для объявления ID: {}", results.size(), adId);

        return comments;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Реализация:
     * <ol>
     *   <li>Извлекает аутентифицированного пользователя через {@link SecurityUtils}</li>
     *   <li>Проверяет существование целевого объявления</li>
     *   <li>Создаёт сущность комментария из DTO через маппер</li>
     *   <li>Устанавливает автора и связь с объявлением</li>
     *   <li>Сохраняет в БД и возвращает DTO созданного комментария</li>
     * </ol>
     *
     * <p>Безопасность: Автор определяется из аутентификации.
     *
     * @param adId идентификатор объявления, к которому добавляется комментарий
     * @param authentication объект аутентификации, содержащий данные текущего пользователя
     * @param dto DTO с текстом создаваемого комментария
     * @return DTO созданного комментария с заполненными полями автора и временными метками
     * @throws AdNotFoundException если объявление с указанным ID не существует
     */
    @Override
    @Transactional
    public Comment addComment(Integer adId, Authentication authentication, CreateOrUpdateComment dto) {

        String email = authentication.getName();
        log.debug("Добавление комментария к объявлению ID: {} пользователем: {}", adId, email);

        // Получаем аутентифицированного пользователя (безопасно через SecurityUtils)
        UserEntity author = SecurityUtils.getAuthenticatedUser(userRepository, authentication);

        // Находим объявление (валидация существования)
        AdEntity ad = adRepository.findById(adId)
                .orElseThrow(() -> {
                            log.warn("Попытка добавления комментария к несуществующему объявлению ID: {}", adId);
                   return new AdNotFoundException(adId);
                        });

        // Создаём и настраиваем сущность комментария
        CommentEntity entity = commentMapper.toEntity(dto);
        entity.setAd(ad);
        entity.setAuthor(author);

        // Сохраняем и возвращаем результат
        CommentEntity savedEntity = commentRepository.save(entity);
        Comment createdComment = commentMapper.toDto(savedEntity);

        log.info("Создан комментарий ID: {} к объявлению ID: {} пользователем {}",
                savedEntity.getId(), adId, email);
        return createdComment;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Проверка прав доступа:
     * <ol>
     *   <li>Находит комментарий по ID</li>
     *   <li>Проверяет, принадлежит ли комментарий указанному объявлению</li>
     *   <li>Проверяет, является ли пользователь автором комментария</li>
     *   <li>Если нет - проверяет роль ADMIN через {@code @PreAuthorize}</li>
     *   <li>Удаляет комментарий</li>
     * </ol>
     *
     * <p>Безопасность: Проверка {@code commentEntity.getAuthor().getEmail().equals(email)}
     */
    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteComment(Integer adId, Integer commentId, Authentication authentication) {

        String email = authentication.getName();
        log.debug("Удаление комментария ID: {} из объявления ID: {} пользователем: {}",
                commentId, adId, email);

        // Находим комментарий (валидация существования)
        CommentEntity commentEntity = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));

        // Валидация: комментарий должен быть от этого объявления
        if (commentEntity.getAd() == null || !adId.equals(commentEntity.getAd().getId())) {
            throw new IllegalArgumentException("Комментарий не принадлежит указанному объявлению");
        }

        if (!commentEntity.getAuthor().getEmail().equals(email)) {
            // Если не автор, то должен быть ADMIN (проверено @PreAuthorize)
            log.debug("Удаление комментария администратором");
        }

        commentRepository.delete(commentEntity);
        log.info("Комментарий ID: {} удалён пользователем {}", commentId, email);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Особенность: Обновляет только текст комментария,
     * дата создания и автор остаются неизменными.
     */
    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public Comment updateComment(Integer adId, Integer commentId,
                                 Authentication authentication, CreateOrUpdateComment dto) {
        String email = authentication.getName();
        log.debug("Обновление комментария ID: {} в объявлении ID: {} пользователем: {}",
                commentId, adId, email);

        // Находим комментарий (валидация существования)
        CommentEntity commentEntity = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));

        // Валидация: комментарий должен принадлежать указанному объявлению
        if (commentEntity.getAd() == null || !adId.equals(commentEntity.getAd().getId())) {
            throw new IllegalArgumentException("Комментарий не принадлежит указанному объявлению");
        }

        if (!commentEntity.getAuthor().getEmail().equals(email)) {

            // Если не автор, то должен быть ADMIN (проверено @PreAuthorize)
            log.debug("Обновление комментария администратором");
        }

        // Обновляем сущность (только текст, через маппер) и сохраняем
        commentMapper.updateEntityFromDto(dto, commentEntity);
        CommentEntity updatedEntity = commentRepository.save(commentEntity);
        Comment updatedComment = commentMapper.toDto(updatedEntity);

        log.info("Комментарий ID: {} обновлён пользователем {}", commentId, email);
        return updatedComment;
    }
}
