package ru.skypro.homework.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;
import ru.skypro.homework.entity.UserEntity;
import ru.skypro.homework.repository.UserRepository;

/**
 * Утилитный класс для работы с аутентификацией и безопасностью.
 * Содержит общие методы для получения аутентифицированного пользователя
 * и обработки ошибок аутентификации.
 *
 * <p>Принцип DRY (Don't Repeat Yourself): централизует логику,
 * которая используется в нескольких сервисах.
 *
 * @see UserRepository
 * @see Authentication
 */
@Slf4j
public class SecurityUtils {

    /**
     * Получает сущность пользователя на основе данных аутентификации.
     * Используется во всех сервисах, где требуется доступ к текущему пользователю.
     *
     * <p>Логика работы:
     * <ol>
     *   <li>Извлекает email из объекта {@link Authentication}</li>
     *   <li>Ищет пользователя в {@link UserRepository} по email</li>
     *   <li>Если пользователь не найден - выбрасывает {@link ResponseStatusException} с 401</li>
     * </ol>
     *
     * <p>Ситуация "пользователь аутентифицирован, но не найден в БД" возможна при:
     * <ul>
     *   <li>Рассинхронизации между UserDetailsManager и UserRepository</li>
     *   <li>Удалении пользователя из БД во время активной сессии</li>
     * </ul>
     *
     * <p>Возвращает HTTP статус 401 (Unauthorized), чтобы фронтенд
     * мог корректно обработать ситуацию (показать форму входа).
     *
     * @param userRepository репозиторий пользователей
     * @param authentication объект аутентификации Spring Security
     * @return сущность аутентифицированного пользователя
     * @throws ResponseStatusException с HTTP 401 если пользователь не найден
     */
    public static UserEntity getAuthenticatedUser(UserRepository userRepository,
                                                  Authentication authentication) {
        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Несогласованность аутентификации: " +
                            "пользователь {} не найден в БД после успешной аутентификации", email);
                    return new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                            "Несоответствие аутентификационных данных. " +
                                    "Пожалуйста, войдите в систему еще раз");
                });
    }

    /**
     * Проверяет, является ли пользователь администратором.
     * Вспомогательный метод для проверки прав доступа.
     *
     * @param authentication объект аутентификации
     * @return true если пользователь имеет роль ADMIN
     */
    public static boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }

    /**
     * Проверяет, имеет ли пользователь доступ к ресурсу.
     * Пользователь имеет доступ если:
     * <ul>
     *   <li>Он автор ресурса (сравнивает email)</li>
     *   <li>Он администратор (роль ADMIN)</li>
     * </ul>
     *
     * @param resourceOwnerEmail email владельца ресурса
     * @param authentication объект аутентификации текущего пользователя
     * @return true если пользователь имеет права на ресурс
     */
    public static boolean hasAccess(String resourceOwnerEmail, Authentication authentication) {
        String currentUserEmail = authentication.getName();

        // Пользователь имеет доступ к своим ресурсам
        if (resourceOwnerEmail.equals(currentUserEmail)) {
            return true;
        }

        // Администратор имеет доступ ко всем ресурсам
        return isAdmin(authentication);
    }
}
