package ru.skypro.homework.service.unit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.skypro.homework.dto.user.NewPassword;
import ru.skypro.homework.dto.user.UpdateUser;
import ru.skypro.homework.dto.user.User;
import ru.skypro.homework.entity.UserEntity;
import ru.skypro.homework.exceptions.InvalidPasswordException;
import ru.skypro.homework.mapper.UserMapper;
import ru.skypro.homework.repository.UserRepository;
import ru.skypro.homework.service.UserService;
import ru.skypro.homework.util.SecurityUtils;

/**
 * Реализация сервиса для работы с пользователями.
 * Обеспечивает бизнес-логику управления профилями пользователей.
 *
 * <p>Использует:
 * <ul>
 *   <li>{@link UserRepository} - для доступа к данным пользователей</li>
 *   <li>{@link UserMapper} - для преобразования Entity <-> DTO</li>
 *   <li>{@link PasswordEncoder} - для шифрования паролей</li>
 * </ul>
 *
 * <p>Все операции выполняются в транзакциях для обеспечения целостности данных.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * {@inheritDoc}
     * <p>
     * Получает информацию о текущем аутентифицированном пользователе.
     * Гарантирует, что пользователь существует в базе данных, так как
     * он уже прошел проверку подлинности Spring Security.
     *
     * @param authentication объект аутентификации Spring Security
     * @return DTO с информацией о текущем пользователе
     * @throws ResponseStatusException (401 согласно openapi) если аутентифицированный
     * пользователь не найден в базе данных (крайний случай).
     */
    @Override
    @Transactional(readOnly = true)
    public User getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        log.debug("Получение информации о пользователе: {}", email);

        UserEntity userEntity = SecurityUtils.getAuthenticatedUser(userRepository, authentication);
        User userDto = userMapper.toDto(userEntity);

        log.trace("Информация о пользователе успешно получена: {}", email);
        return userDto;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Обновляет профиль текущего аутентифицированного пользователя.
     * <p>Обновляет только переданные поля (частичное обновление):
     * <ul>
     *   <li>Имя (firstName)</li>
     *   <li>Фамилия (lastName)</li>
     *   <li>Телефон (phone)</li>
     * </ul>
     *
     * @param authentication объект аутентификации Spring Security
     * @param updateUser DTO с обновленными данными профиля
     * @return обновленный DTO пользователя (те же данные, что были переданы)
     * @throws ResponseStatusException (401 согласно openapi) если аутентифицированный
     * пользователь не найден в базе данных (крайний случай).
     */
    @Override
    @Transactional
    public UpdateUser updateUser(Authentication authentication, UpdateUser updateUser) {
        String email = authentication.getName();
        log.debug("Обновление профиля пользователя: {}", email);
        log.trace("Данные для обновления: firstName={}, lastName={}, phone={}",
                updateUser.getFirstName(), updateUser.getLastName(), updateUser.getPhone());

        UserEntity userEntity =SecurityUtils.getAuthenticatedUser(userRepository, authentication);

        userMapper.updateEntityFromUpdateUser(updateUser, userEntity);
        userRepository.save(userEntity);

        log.info("Профиль пользователя {} успешно обновлен", email);
        return updateUser;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Обновляет пароль текущего аутентифицированного пользователя.
     * <p>Требования безопасности:
     * <ol>
     *   <li>Текущий пароль должен быть указан верно</li>
     *   <li>Новый пароль хешируется перед сохранением</li>
     *   <li>В БД хранится только хеш пароля, никогда не сохраняется открытый текст</li>
     * </ol>
     *
     * <p>Алгоритм работы:
     * <ol>
     *   <li>Извлекает пользователя по email из {@link Authentication}</li>
     *   <li>Проверяет, что текущий пароль совпадает через {@link PasswordEncoder#matches}</li>
     *   <li>Хеширует новый пароль через {@link PasswordEncoder#encode}</li>
     *   <li>Сохраняет обновленного пользователя в БД</li>
     * </ol>
     *
     * @param authentication объект аутентификации Spring Security
     * @param newPassword DTO с текущим и новым паролями
     * @throws InvalidPasswordException если текущий пароль указан неверно
     * @throws ResponseStatusException (401 согласно openapi) если аутентифицированный
     * пользователь не найден в базе данных (крайний случай).
     */
    @Override
    @Transactional
    public void updatePassword(Authentication authentication, NewPassword newPassword) {
        String email = authentication.getName();
        log.debug("Смена пароля для пользователя: {}", email);

        UserEntity userEntity = SecurityUtils.getAuthenticatedUser(userRepository, authentication);

        /* Проверяем текущий пароль */
        if (!passwordEncoder.matches(newPassword.getCurrentPassword(), userEntity.getPassword())) {
            log.warn("Неверный текущий пароль для пользователя: {}", email);
            throw new InvalidPasswordException();
        }

        /*  Хешируем и сохраняем новый пароль */
        String encodedNewPassword = passwordEncoder.encode(newPassword.getNewPassword());
        userEntity.setPassword(encodedNewPassword);
        userRepository.save(userEntity);

        log.info("Пароль для пользователя {} успешно изменен", email);
    }

}
