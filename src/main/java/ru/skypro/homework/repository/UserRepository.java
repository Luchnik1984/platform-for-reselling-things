package ru.skypro.homework.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.skypro.homework.entity.UserEntity;

import java.util.Optional;

/**
 * Репозиторий для работы с пользователями.
 * Обеспечивает доступ к данным в таблице 'users'.
 *
 * <p>Наследует {@link JpaRepository}, что предоставляет:
 * <ul>
 *   <li>CRUD операции: save(), findById(), findAll(), delete()</li>
 *   <li>Пагинацию и сортировку</li>
 *   <li>Автоматическую генерацию SQL запросов</li>
 * </ul>
 *
 * <p>Кастомные методы (по соглашению Spring Data JPA):
 * <ul>
 *   <li>findByEmail(String email) → SELECT * FROM users WHERE email = ?</li>
 *   <li>existsByEmail(String email) → SELECT COUNT(*) > 0 FROM users WHERE email = ?</li>
 * </ul>
 *
 * @see UserEntity
 */
@Repository
public interface UserRepository extends JpaRepository<UserEntity, Integer> {

    /**
     * Находит пользователя по email.
     * Используется для аутентификации и проверки уникальности.
     *
     * @param email email пользователя
     * @return Optional с пользователем, если найден
     */
    Optional<UserEntity> findByEmail(String email);

    /**
     * Проверяет существование пользователя с указанным email.
     * Используется при регистрации для проверки уникальности.
     *
     * @param email email для проверки
     * @return true если пользователь с таким email существует
     */
    boolean existsByEmail(String email);
}
