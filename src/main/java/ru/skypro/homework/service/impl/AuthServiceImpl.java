package ru.skypro.homework.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import ru.skypro.homework.service.AuthService;

/**
 * Сервис для работы с аутентификацией.
 * Использует JdbcUserDetailsManager через AuthenticationManager.
 *
 * <p>Основная функция: Проверка учетных данных для эндпоинта /login.
 * JdbcUserDetailsManager автоматически загружает пользователей из БД.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {


    private final AuthenticationManager authenticationManager;

    /**
     * Проверяет учетные данные пользователя.
     * Использует AuthenticationManager, который делегирует JdbcUserDetailsManager.
     *
     * <p>Алгоритм:
     * <ol></ol>
     * <li> Создает UsernamePasswordAuthenticationToken</li>
     * <li> Передает в AuthenticationManager</li>
     * <li> AuthenticationManager использует JdbcUserDetailsManager для проверки</li>
     * </ol>
     *
     * @param username email пользователя
     * @param password пароль пользователя
     * @return true если учетные данные верны
     */
    @Override
    public boolean login(String username, String password) {
        log.debug("Проверка учетных данных для пользователя: {}", username);

        try {
            // Создаем токен аутентификации
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(username, password);

            log.trace("Токен аутентификации создан для: {}", username);

            // AuthenticationManager проверяет токен через JdbcUserDetailsManager
            Authentication authentication = authenticationManager.authenticate(authToken);

            // Проверяем результат
            boolean isAuthenticated = authentication.isAuthenticated();

            if (isAuthenticated) {
                log.debug("Аутентификация успешна для пользователя: {}", username);
            } else {
                log.warn("Аутентификация не удалась для пользователя: {}", username);
            }
            return isAuthenticated;

        } catch (BadCredentialsException e) {
            log.warn("Неверные учетные данные для пользователя: {}", username);
            return false;
        } catch (Exception e) {

            log.error("Ошибка при аутентификации пользователя {}: {}",
                    username, e.getMessage(), e);
            return false;
        }
    }
}
