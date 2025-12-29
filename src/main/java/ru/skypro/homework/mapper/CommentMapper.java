package ru.skypro.homework.mapper;

import ru.skypro.homework.dto.comments.Comment;
import ru.skypro.homework.dto.comments.CreateOrUpdateComment;
import ru.skypro.homework.entity.CommentEntity;


/**
 * Маппер для преобразования между сущностью комментария и DTO.
 *
 * <p>Преобразования:
 * <ul>
 *   <li>{@link CreateOrUpdateComment} → {@link CommentEntity} (при создании/обновлении)</li>
 *   <li>{@link CommentEntity} → {@link Comment} (для отображения)</li>
 * </ul>
 *
 * @see CommentEntity
 * @see CreateOrUpdateComment
 * @see Comment
 */

public interface CommentMapper {

    /**
     * Преобразует DTO комментария в сущность.
     *
     * @param dto DTO с текстом комментария
     * @return сущность комментария
     */

    CommentEntity toEntity(CreateOrUpdateComment dto);

    /**
     * Преобразует сущность комментария в DTO.
     *
     * <p>Особенности:
     * <ul>
     *   <li>Преобразует {@link java.time.LocalDateTime} в миллисекунды (Unix timestamp)</li>
     *   <li>Берёт имя автора и ссылку на аватар из связанного {@link ru.skypro.homework.entity.UserEntity}</li>
     * </ul>
     *
     * @param commentEntity сущность комментария
     * @return DTO комментария
     */

    // @Mapping(target = "text", source = "text")
    Comment toDto(CommentEntity commentEntity);

    /**
     * Обновляет сущность комментария из DTO.
     *
     * @param dto DTO с обновлённым текстом
     * @param commentEntity сущность для обновления
     */

    void updateEntityFromDto(CreateOrUpdateComment dto, CommentEntity commentEntity);

}
