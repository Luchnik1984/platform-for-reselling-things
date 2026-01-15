package ru.skypro.homework.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.skypro.homework.config.AdminConfig;
import ru.skypro.homework.dto.reg.Register;
import ru.skypro.homework.entity.UserEntity;
import ru.skypro.homework.enums.Role;
import ru.skypro.homework.mapper.UserMapper;
import ru.skypro.homework.repository.UserRepository;
import ru.skypro.homework.service.RegService;

/**
 * Сервис для регистрации новых пользователей.
 * Сохраняет пользователей в БД в формате, понятном JdbcUserDetailsManager.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RegServiceImpl implements RegService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final AdminConfig adminConfig;


    /**
     * Регистрирует нового пользователя.
     *
     * <p>Важно для совместимости с JdbcUserDetailsManager:
     * <ol>
     *  <li> Пароль хешируется тем же PasswordEncoder (BCrypt)</li>
     *  <li> Поле enabled = true (иначе пользователь не сможет залогиниться)</li>
     *  <li> Email сохраняется как есть (JdbcUserDetailsManager ищет по email)</li>
     * </ol>
     * <p>
     * Особенности безопасности:
     * 1. Всегда проверяет уникальность email
     * 2. Для регистрации как ADMIN требуется:
     *    - Либо валидный adminCode из конфигурации
     *    - Либо email из whitelist
     * 3. Иначе автоматически понижается до USER
     * </p>
     */
    @Override
    @Transactional
    public boolean register(Register register) {

        String email = register.getUsername();
        log.info("Начало регистрации пользователя: {}", email);

        // 1. Проверка уникальности email
        if (userRepository.existsByEmail(email)) {
            log.warn("Email уже существует: {}", email);
            return false;
        }

        // Определяем конечную роль с проверкой безопасности
        Role finalRole = determineFinalRole(register);

        // Обновляем роль в DTO для маппера
        register.setRole(finalRole);

        // Преобразование DTO → Entity (без пароля)
        UserEntity userEntity = userMapper.toEntity(register);

        // 3. Хеширование пароля (совместимо с JdbcUserDetailsManager)
        String rawPassword = register.getPassword();
        String hashedPassword = passwordEncoder.encode(rawPassword);
        userEntity.setPassword(hashedPassword);

        log.trace("Пароль захеширован для пользователя: {}", email);

        // 4. Гарантируем, что пользователь активен
        userEntity.setEnabled(true);

        // 5. Сохраняем в БД
        userRepository.save(userEntity);

        log.info("Пользователь успешно зарегистрирован: {}", email);
        return true;
    }

    /**
     * Определяет конечную роль пользователя с проверкой безопасности.
     */
    private Role determineFinalRole(Register register) {
        String email = register.getUsername();
        Role requestedRole = register.getRole() != null ? register.getRole() : Role.USER;

        // Если запрошена роль USER - всегда разрешаем
        if (requestedRole == Role.USER) {
            return Role.USER;
        }

        // Если запрошена роль ADMIN - проверяем права
        if (requestedRole == Role.ADMIN) {
            boolean canBeAdmin = checkAdminRegistrationRights(email, register.getAdminCode());

            if (canBeAdmin) {
                log.info("Админ одобрен для регистрации: {}", email);
                return Role.ADMIN;
            } else {
                log.warn("Попытка регистрации ADMIN без прав: {}. Понижен до USER.", email);
                return Role.USER;  // Автоматическое понижение
            }
        }

        // Fallback
        return Role.USER;
    }

    /**
     * Проверяет права для регистрации как ADMIN.
     * Два способа стать админом:
     * 1. Валидный adminCode из конфигурации
     * 2. Email в whitelist (независимо от кода)
     */
    private boolean checkAdminRegistrationRights(String email, String providedAdminCode) {
        // Способ 1: Email в белом списке
        if (adminConfig.isEmailInWhitelist(email)) {
            log.debug("Email {} находится в whitelist для ADMIN", email);
            return true;
        }

        // Способ 2: Валидный adminCode
        if (adminConfig.isValidAdminCode(providedAdminCode)) {
            log.debug("Предоставлен валидный adminCode для {}", email);
            return true;
        }

        log.debug("Нет прав для регистрации как ADMIN: {}", email);
        return false;
    }

}
