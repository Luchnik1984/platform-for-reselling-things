package ru.skypro.homework.service.unit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.skypro.homework.dto.reg.Register;
import ru.skypro.homework.entity.UserEntity;
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


    /**
     * Регистрирует нового пользователя.
     *
     * <p>Важно для совместимости с JdbcUserDetailsManager:
     * <ol>
     *  <li> Пароль хешируется тем же PasswordEncoder (BCrypt)</li>
     *  <li> Поле enabled = true (иначе пользователь не сможет залогиниться)</li>
     *  <li> Email сохраняется как есть (JdbcUserDetailsManager ищет по email)</li>
     * </ol>
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

        // 2. Преобразование DTO → Entity (без пароля)
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

}
