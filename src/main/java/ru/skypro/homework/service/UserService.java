package ru.skypro.homework.service;

import org.springframework.security.core.Authentication;
import ru.skypro.homework.dto.user.NewPassword;
import ru.skypro.homework.dto.user.UpdateUser;
import ru.skypro.homework.dto.user.User;

/**
 * Сервис для управления пользователями.
 * Обеспечивает бизнес-логику работы с профилями пользователей.
 */
public interface UserService {
    /**
     * Получает информацию о текущем аутентифицированном пользователе.
     *
     * @param authentication объект аутентификации Spring Security
     * @return DTO с информацией о пользователе
     */
    User getCurrentUser(Authentication authentication);

    /**
     * Обновляет профиль текущего пользователя.
     * Обновляет только переданные поля (частичное обновление).
     *
     * @param authentication объект аутентификации
     * @param updateUser DTO с обновленными данными
     * @return обновленный DTO пользователя
     */
    UpdateUser updateUser(Authentication authentication, UpdateUser updateUser);

    /**
     * Обновляет пароль текущего пользователя.
     * Проверяет старый пароль перед установкой нового.
     *
     * @param authentication объект аутентификации
     * @param newPassword DTO с текущим и новым паролями
     */
    void updatePassword(Authentication authentication, NewPassword newPassword);
}
