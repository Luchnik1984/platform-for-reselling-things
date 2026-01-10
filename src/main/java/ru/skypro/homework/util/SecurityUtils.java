package ru.skypro.homework.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;
import ru.skypro.homework.entity.UserEntity;
import ru.skypro.homework.repository.UserRepository;

/**
 * Утилитарный класс для работы с аутентификацией и безопасностью.
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
     * Проверяет, имеет ли аутентифицированный пользователь роль ADMIN.
     * Используется для проверки прав доступа в бизнес-логике.
     *
     * <p>Алгоритм работы:
     * <ol>
     *   <li>Извлекает authorities из объекта {@link Authentication}</li>
     *   <li>Ищет authority с именем "ROLE_ADMIN"</li>
     *   <li>Возвращает true если роль найдена</li>
     * </ol>
     *
     * <p>Соответствие ролям в системе:
     * <ul>
     *   <li>USER -> authorities: ["ROLE_USER"]</li>
     *   <li>ADMIN -> authorities: ["ROLE_ADMIN", "ROLE_USER"] (обычно)</li>
     * </ul>
     *
     * @param authentication объект аутентификации Spring Security
     * @return true если пользователь имеет роль ADMIN, иначе false
     */
    public static boolean isAdmin(Authentication authentication) {
        if (authentication == null) {
            log.warn("Попытка проверки роли ADMIN для null аутентификации");
            return false;
        }

        if (authentication.getAuthorities() == null) {
            log.warn("Аутентификация не содержит authorities: {}", authentication);
            return false;
        }

        return authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority ->
                        "ROLE_ADMIN".equals(grantedAuthority.getAuthority()));
    }

}
