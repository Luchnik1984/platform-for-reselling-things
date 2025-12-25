package ru.skypro.homework.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.skypro.homework.entity.CommentEntity;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с комментариями.
 * Обеспечивает доступ к данным в таблице 'comments'.
 *
 * <p>Особенности:
 * <ul>
 *   <li>Фильтрация по объявлению (для GET /ads/{id}/comments)</li>
 *   <li>Проверка прав доступа (автор комментария)</li>
 *   <li>Eager загрузка автора для отображения имени и аватара</li>
 * </ul>
 *
 * @see CommentEntity
 */
@Repository
public interface CommentRepository extends JpaRepository<CommentEntity, Integer> {

    /**
     * Находит все комментарии указанного объявления.
     * Использует EntityGraph для загрузки авторов комментариев.
     *
     * @param adId идентификатор объявления
     * @return список комментариев с загруженными авторами
     */
    @EntityGraph(attributePaths = {"author"})
    List<CommentEntity> findAllByAdId(Integer adId);

    /**
     * Находит комментарий по идентификатору и автору.
     * Используется для проверки прав доступа при обновлении/удалении.
     *
     * @param id       идентификатор комментария
     * @param authorId идентификатор автора
     * @return Optional с комментарием, если найдено и принадлежит автору
     */
    Optional<CommentEntity> findByIdAndAuthorId(Integer id, Integer authorId);
}

