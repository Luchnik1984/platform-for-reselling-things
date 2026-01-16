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
     * 2. Для регистрации как ADMIN требуется email из whitelist
     * 3. Иначе автоматически понижается до USER
     * </p>
     */
    @Override
    @Transactional
    public boolean register(Register register) {

        String email = register.getUsername();
        String rawPassword = register.getPassword();
        log.info("Начало регистрации пользователя: {}", email);

        log.debug("Конфигурация администратора: enabled={},  whitelist={}",
                adminConfig.isEnabled(), adminConfig.getEmailWhitelist());


        // 1. Проверка уникальности email
        if (userRepository.existsByEmail(email)) {
            log.warn("Email уже существует: {}", email);
            return false;
        }

        // Определяем конечную роль с проверкой безопасности
        Role finalRole = determineFinalRole(email, register.getRole());

        // Обновляем роль в DTO для маппера
        register.setRole(finalRole);

        // Преобразование DTO → Entity (без пароля)
        UserEntity userEntity = userMapper.toEntity(register);

        // 3. Хеширование пароля (совместимо с JdbcUserDetailsManager)

        String hashedPassword = passwordEncoder.encode(rawPassword);
        userEntity.setPassword(hashedPassword);
        log.trace("Пароль захеширован для пользователя: {}", email);

        // Гарантируем, что пользователь активен
        userEntity.setEnabled(true);

        // Сохраняем в БД
        userRepository.save(userEntity);

        log.info("Пользователь успешно зарегистрирован: {} с ролью {}", email, finalRole);
        return true;
    }

    /**
     * Определяет конечную роль пользователя.
     * ADMIN только если email в whitelist, иначе USER.
     */
    private Role determineFinalRole(String email, Role requestedRole) {
        if (requestedRole == null) {
            requestedRole = Role.USER;
        }

        log.debug("Запрошена роль: {} для пользователя: {}", requestedRole, email);

        // Если запрошена роль USER - всегда разрешаем
        if (requestedRole == Role.USER) {
            log.debug("Запрошена роль USER, разрешаем без проверок");
            return Role.USER;
        }

        // Если запрошена роль ADMIN - проверяем whitelist
        if (requestedRole == Role.ADMIN) {
            boolean canBeAdmin = checkAdminRegistrationRights(email);

            if (canBeAdmin) {
                log.info("Админ одобрен для регистрации: {}", email);
                return Role.ADMIN;
            } else {
                log.warn("Попытка регистрации ADMIN без прав: {}. Понижен до USER.", email);
                return Role.USER;  // Автоматическое понижение
            }
        }

        return Role.USER;
    }

    /**
     * Проверяет права для регистрации как ADMIN.
     * Только через whitelist.
     */
    private boolean checkAdminRegistrationRights(String email) {
        // Проверяем, включена ли регистрация администраторов
        if (!adminConfig.isEnabled()) {
            log.debug("Регистрация администраторов отключена в конфигурации");
            return false;
        }

        log.debug("Проверка whitelist для: {}", email);
        log.debug("Whitelist: {}", adminConfig.getEmailWhitelist());

        // Проверяем email в белом списке
        if (adminConfig.isEmailInWhitelist(email)) {
            log.debug("Email {} находится в whitelist для ADMIN", email);
            return true;
        }

        log.debug("Email {} НЕ в whitelist", email);
        return false;
    }

}
