package ru.skypro.homework.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.skypro.homework.entity.ImageEntity;

import java.util.Optional;

/**
 * Репозиторий для работы с изображениями.
 * Обеспечивает доступ к данным в таблице 'images'.
 *
 * <p>Особенности:
 * <ul>
 *   <li>Поиск по связанным сущностям (пользователь, объявление)</li>
 *   <li>Проверка существования изображений</li>
 *   <li>Удаление по связанной сущности</li>
 * </ul>
 *
 * @see ImageEntity
 */
@Repository
public interface ImageRepository extends JpaRepository<ImageEntity, Integer> {

    /**
     * Находит изображение пользователя (аватар).
     *
     * @param userId идентификатор пользователя
     * @return Optional с изображением, если найдено
     */
    Optional<ImageEntity> findByUserId(Integer userId);

    /**
     * Находит изображение объявления.
     *
     * @param adId идентификатор объявления
     * @return Optional с изображением, если найдено
     */
    Optional<ImageEntity> findByAdId(Integer adId);

    /**
     * Удаляет аватар пользователя.
     *
     * @param userId идентификатор пользователя
     */
    void deleteByUserId(Integer userId);

    /**
     * Удаляет изображение объявления.
     *
     * @param adId идентификатор объявления
     */
    void deleteByAdId(Integer adId);
}

