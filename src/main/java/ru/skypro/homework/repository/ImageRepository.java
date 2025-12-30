package ru.skypro.homework.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.skypro.homework.entity.ImageEntity;

/**
 * Репозиторий для работы с изображениями.
 * Обеспечивает базовые CRUD операции для таблицы 'images'.
 *
 * <p>После изменения структуры сущностей управление изображениями
 * осуществляется через UserEntity и AdEntity.
 */
@Repository
public interface ImageRepository extends JpaRepository<ImageEntity, Integer> {

}

