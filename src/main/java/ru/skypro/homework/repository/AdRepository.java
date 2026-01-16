package ru.skypro.homework.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.skypro.homework.entity.AdEntity;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с объявлениями.
 * Обеспечивает доступ к данным в таблице 'ads'.
 *
 * <p>Особенности:
 * <ul>
 *   <li>Использует {@link EntityGraph} для eager загрузки связей</li>
 *   <li>Содержит кастомные методы для фильтрации по автору</li>
 *   <li>Поддерживает пагинацию через наследование от JpaRepository</li>
 * </ul>
 *
 * @see AdEntity
 */
@Repository
public interface AdRepository extends JpaRepository<AdEntity, Integer> {

    /**
     * Находит все объявления, загружая автора и изображение.
     * Использует EntityGraph для решения проблемы N+1 запросов.
     *
     * @return список всех объявлений с загруженными связями
     */
    @EntityGraph(attributePaths = {"author", "image"})
    @Override
    List<AdEntity> findAll();

    /**
     * Находит объявления конкретного автора.
     * Используется для эндпоинта GET /ads/me.
     *
     * @param authorId идентификатор автора
     * @return список объявлений указанного автора
     */
    @EntityGraph(attributePaths = {"author"})
    List<AdEntity> findAllByAuthorId(Integer authorId);

    /**
     * Находит объявление по идентификатору и автору.
     * Используется для проверки прав доступа при обновлении/удалении.
     *
     * @param id       идентификатор объявления
     * @param authorId идентификатор автора
     * @return Optional с объявлением, если найдено и принадлежит автору
     */
    Optional<AdEntity> findByIdAndAuthorId(Integer id, Integer authorId);
}
