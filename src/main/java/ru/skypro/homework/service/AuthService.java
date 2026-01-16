package ru.skypro.homework.service;

import ru.skypro.homework.config.WebSecurityConfig;

/**
 * Сервис для работы с аутентификацией пользователей.
 * Предоставляет методы для проверки учетных данных.
 *
 * <p>Основное назначение:
 * <ul>
 *   <li>Поддержка эндпоинта {@code POST /login} для совместимости с OpenAPI</li>
 *   <li>Проверка учетных данных вне контекста HTTP-запроса</li>
 *   <li>Интеграция с Spring Security AuthenticationManager</li>
 * </ul>
 *
 * <p>Примечание: Основная аутентификация в приложении работает через
 * HTTP Basic Authentication, настроенное в {@link WebSecurityConfig}.
 * Данный сервис используется для дополнительного эндпоинта {@code /login}.
 */
public interface AuthService {

    boolean login(String userName, String password);

}
