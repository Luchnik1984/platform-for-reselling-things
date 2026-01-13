package ru.skypro.homework.service.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import ru.skypro.homework.AbstractIntegrationTest;
import ru.skypro.homework.dto.user.NewPassword;
import ru.skypro.homework.dto.user.UpdateUser;
import ru.skypro.homework.dto.user.User;
import ru.skypro.homework.entity.UserEntity;
import ru.skypro.homework.enums.Role;
import ru.skypro.homework.exceptions.InvalidPasswordException;
import ru.skypro.homework.repository.UserRepository;
import ru.skypro.homework.service.UserService;
import ru.skypro.homework.service.unit.UserServiceImpl;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Интеграционные тесты для {@link UserService}.
 * <p>
 * <b>Цель:</b> Проверка бизнес-логики сервиса пользователей в условиях,
 * максимально приближенных к реальным, с использованием:
 * <ul>
 *   <li>Контейнера PostgreSQL через Testcontainers</li>
 *   <li>Полного контекста Spring приложения</li>
 *   <li>Применённых миграций Flyway</li>
 *   <li>Транзакционной семантики</li>
 * </ul>
 *
 * <p><b>Архитектура тестирования:</b>
 * <ol>
 *   <li>Наследование от {@link AbstractIntegrationTest} для общей конфигурации контейнера</li>
 *   <li>Использование {@code @BeforeEach} для подготовки тестовых данных</li>
 *   <li>Активный профиль "docker-test" для изоляции от продакшн БД</li>
 *   <li>Автоматический rollback транзакций после каждого теста</li>
 * </ol>
 *
 * <p><b>Основные сценарии тестирования:</b>
 * <ul>
 *   <li>Получение профиля текущего пользователя</li>
 *   <li>Обновление информации профиля (частичное обновление)</li>
 *   <li>Смена пароля с проверкой текущего пароля</li>
 *   <li>Обработка исключений при неверных данных</li>
 * </ul>
 *
 * @see UserService
 * @see UserServiceImpl
 * @see AbstractIntegrationTest
 */
@Transactional
@DisplayName("Интеграционные тесты UserService с Testcontainers")
class UserServiceIntegrationTest extends AbstractIntegrationTest {

    /**
     * Сервис пользователей, который тестируем.
     * Используется для вызова тестируемых методов бизнес-логики.
     */
    @Autowired
    private UserService userService;

    /**
     * Репозиторий пользователей для подготовки тестовых данных.
     * <p>
     * <b>Назначение в тестах:</b>
     * <ol>
     *   <li>Создание тестовых пользователей перед каждым тестом</li>
     *   <li>Проверка состояния БД после выполнения операций сервиса</li>
     *   <li>Очистка тестовых данных через транзакционный rollback</li>
     * </ol>
     */
    @Autowired
    private UserRepository userRepository;

    /**
     * Кодировщик паролей для проверки хеширования.
     * <p>
     * <b>Важно:</b> Используется тот же PasswordEncoder (BCrypt),
     * что и в основном приложении, для корректной проверки паролей.
     */
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Тестовый пользователь USER.
     * <p>
     * Создаётся перед каждым тестом в методе {@link #setUp()}.
     * Используется в большинстве тестов для симуляции аутентифицированного пользователя.
     */
    private UserEntity testUser;

    /**
     * Идентификатор сохранённого тестового пользователя.
     * <p>
     * Сохраняется для последующего сравнения в assertions.
     */
    private Integer savedUserId;

    /**
     * Подготовка тестовых данных перед каждым тестом.
     * <p>
     * <b>Выполняет:</b>
     * <ol>
     *   <li>Очистку таблиц (через каскадное удаление или явный deleteAll)</li>
     *   <li>Создание тестового пользователя с ролью USER</li>
     *   <li>Хеширование пароля с использованием того же PasswordEncoder</li>
     *   <li>Сохранение пользователя в БД</li>
     * </ol>
     *
     * <p><b>Транзакционность:</b> Метод выполняется в транзакции, которая
     * автоматически откатывается после каждого теста благодаря {@code @Transactional}.
     */
    @BeforeEach
    void setUp() {
        // Очищаем таблицы перед тестом (каскадно через deleteAll)
        userRepository.deleteAll();

        // Создаём тестового пользователя с хешированным паролем
        String rawPassword = "password123";
        String hashedPassword = passwordEncoder.encode(rawPassword);

        testUser = new UserEntity(
                "test.user@example.com",
                hashedPassword,
                "Иван",
                "Иванов",
                "+7 (999) 111-22-33",
                Role.USER
        );
        testUser.setEnabled(true);

        UserEntity savedUser = userRepository.save(testUser);
        savedUserId = savedUser.getId();
    }

    /**
     * Тест успешного получения профиля текущего аутентифицированного пользователя.
     * <p>
     * <b>Сценарий:</b> Пользователь аутентифицирован и существует в БД.
     * <p>
     * <b>Проверяемые аспекты:</b>
     * <ul>
     *   <li>Корректность преобразования Entity → DTO через маппер</li>
     *   <li>Совпадение всех полей пользователя</li>
     *   <li>Обработка null для поля image (аватар отсутствует)</li>
     *   <li>Верное определение роли пользователя</li>
     * </ul>
     *
     * <p><b>Ожидаемый результат:</b> User DTO со всеми корректными полями.
     */
    @Test
    @DisplayName("Успешное получение профиля текущего пользователя")
    void getCurrentUser_shouldReturnUserDto_whenUserAuthenticated() {
        // Arrange: Создаём объект аутентификации для тестового пользователя
        Authentication authentication = createAuthenticationForUser(testUser, "password123");

        // Act: Вызываем тестируемый метод сервиса
        User result = userService.getCurrentUser(authentication);

        // Assert: Проверяем корректность результата
        assertNotNull(result, "Результат не должен быть null");
        assertEquals(savedUserId, result.getId(), "ID пользователя должен совпадать");
        assertEquals(testUser.getEmail(), result.getEmail(), "Email должен совпадать");
        assertEquals(testUser.getFirstName(), result.getFirstName(), "Имя должно совпадать");
        assertEquals(testUser.getLastName(), result.getLastName(), "Фамилия должна совпадать");
        assertEquals(testUser.getPhone(), result.getPhone(), "Телефон должен совпадать");
        assertEquals(testUser.getRole(), result.getRole(), "Роль должна совпадать");
        assertNull(result.getImage(), "Изображение должно быть null для тестового пользователя");
    }

    /**
     * Тест частичного обновления профиля пользователя.
     * <p>
     * <b>Сценарий:</b> Пользователь обновляет только имя, оставляя другие поля без изменений.
     * <p>
     * <b>Проверяемые аспекты:</b>
     * <ul>
     *   <li>Частичное обновление (только указанные поля)</li>
     *   <li>Сохранение неизменённых полей</li>
     *   <li>Корректность возвращаемого DTO</li>
     *   <li>Фактическое обновление данных в БД</li>
     * </ul>
     *
     * <p><b>Ожидаемый результат:</b> Обновлённый профиль с изменённым именем,
     * остальные поля остаются прежними.
     */
    @Test
    @DisplayName("Частичное обновление профиля пользователя")
    void updateUser_shouldUpdateOnlyProvidedFields_whenPartialUpdate() {
        // Arrange: Создаём аутентификацию и DTO для обновления
        Authentication authentication = createAuthenticationForUser(testUser, "password123");

        UpdateUser updateUser = new UpdateUser();
        updateUser.setFirstName("Алексей"); // Меняем только имя
        updateUser.setLastName(null); // Фамилия не обновляется
        updateUser.setPhone(null); // Телефон не обновляется

        // Act: Вызываем метод обновления профиля
        UpdateUser result = userService.updateUser(authentication, updateUser);

        // Assert: Проверяем возвращаемый DTO
        assertEquals("Алексей", result.getFirstName(), "Имя должно быть обновлено в DTO");
        assertNull(result.getLastName(), "Фамилия должна быть null в DTO");
        assertNull(result.getPhone(), "Телефон должен быть null в DTO");

        // Assert: Проверяем фактическое состояние в БД
        UserEntity updatedUser = userRepository.findById(savedUserId)
                .orElseThrow(() -> new AssertionError("Пользователь должен существовать в БД"));

        assertEquals("Алексей", updatedUser.getFirstName(), "Имя должно быть обновлено в БД");
        assertEquals("Иванов", updatedUser.getLastName(), "Фамилия должна остаться прежней");
        assertEquals("+7 (999) 111-22-33", updatedUser.getPhone(), "Телефон должен остаться прежним");
        assertEquals(testUser.getEmail(), updatedUser.getEmail(), "Email не должен изменяться");
        assertEquals(testUser.getRole(), updatedUser.getRole(), "Роль не должна изменяться");
    }

    /**
     * Тест успешной смены пароля при верном текущем пароле.
     * <p>
     * <b>Сценарий:</b> Пользователь указывает верный текущий пароль и новый пароль.
     * <p>
     * <b>Проверяемые аспекты:</b>
     * <ul>
     *   <li>Валидация текущего пароля через PasswordEncoder.matches()</li>
     *   <li>Хеширование нового пароля перед сохранением</li>
     *   <li>Сохранение хеша нового пароля в БД</li>
     *   <li>Невозможность аутентификации старым паролем после смены</li>
     * </ul>
     *
     * <p><b>Ожидаемый результат:</b> Пароль успешно изменён, новый пароль работает для аутентификации.
     */
    @Test
    @DisplayName("Успешная смена пароля при верном текущем пароле")
    void updatePassword_shouldUpdatePassword_whenCurrentPasswordIsCorrect() {
        // Arrange: Создаём аутентификацию и DTO смены пароля
        Authentication authentication = createAuthenticationForUser(testUser, "password123");

        NewPassword newPassword = new NewPassword();
        newPassword.setCurrentPassword("password123"); // Верный текущий пароль
        newPassword.setNewPassword("newSecurePassword456"); // Новый пароль

        // Сохраняем старый хеш для последующего сравнения
        String oldPasswordHash = testUser.getPassword();

        // Act: Вызываем метод смены пароля
        userService.updatePassword(authentication, newPassword);

        // Assert: Проверяем, что пароль изменился в БД
        UserEntity updatedUser = userRepository.findById(savedUserId)
                .orElseThrow(() -> new AssertionError("Пользователь должен существовать в БД"));

        assertNotEquals(oldPasswordHash, updatedUser.getPassword(),
                "Хеш пароля должен измениться после обновления");

        // Assert: Проверяем, что новый пароль корректно хеширован и работает
        assertTrue(passwordEncoder.matches("newSecurePassword456", updatedUser.getPassword()),
                "Новый пароль должен проходить проверку через PasswordEncoder");

        // Assert: Проверяем, что старый пароль больше не работает
        assertFalse(passwordEncoder.matches("password123", updatedUser.getPassword()),
                "Старый пароль не должен проходить проверку после смены");
    }

    /**
     * Тест обработки неверного текущего пароля при смене пароля.
     * <p>
     * <b>Сценарий:</b> Пользователь указывает неверный текущий пароль.
     * <p>
     * <b>Проверяемые аспекты:</b>
     * <ul>
     *   <li>Выбрасывание {@link InvalidPasswordException}</li>
     *   <li>Сохранение исходного пароля в БД (без изменений)</li>
     *   <li>Корректный HTTP статус 400 через обработчик исключений</li>
     * </ul>
     *
     * <p><b>Ожидаемый результат:</b> {@link InvalidPasswordException} и неизменённый пароль в БД.
     */
    @Test
    @DisplayName("Исключение при смене пароля с неверным текущим паролем")
    void updatePassword_shouldThrowInvalidPasswordException_whenCurrentPasswordIsWrong() {
        // Arrange: Создаём аутентификацию и DTO с неверным текущим паролем
        Authentication authentication = createAuthenticationForUser(testUser, "password123");

        NewPassword newPassword = new NewPassword();
        newPassword.setCurrentPassword("wrongPassword"); // Неверный текущий пароль
        newPassword.setNewPassword("newPassword456");

        // Сохраняем исходный хеш пароля для проверки неизменности
        String originalPasswordHash = testUser.getPassword();

        // Act & Assert: Проверяем, что исключение выбрасывается
        assertThrows(InvalidPasswordException.class, () -> userService.updatePassword(authentication, newPassword), "Должно быть выброшено InvalidPasswordException при неверном текущем пароле");

        // Assert: Проверяем, что пароль в БД не изменился
        UserEntity unchangedUser = userRepository.findById(savedUserId)
                .orElseThrow(() -> new AssertionError("Пользователь должен существовать в БД"));

        assertEquals(originalPasswordHash, unchangedUser.getPassword(),
                "Пароль в БД не должен изменяться при неверном текущем пароле");
    }

    /**
     * Тест обновления всех полей профиля пользователя.
     * <p>
     * <b>Сценарий:</b> Пользователь обновляет все доступные поля профиля.
     * <p>
     * <b>Проверяемые аспекты:</b>
     * <ul>
     *   <li>Обновление имени, фамилии и телефона одновременно</li>
     *   <li>Невозможность изменения email и role через этот метод</li>
     *   <li>Корректность возвращаемого DTO</li>
     * </ul>
     *
     * <p><b>Ожидаемый результат:</b> Все указанные поля обновлены,
     * email и role остаются неизменными.
     */
    @Test
    @DisplayName("Обновление всех полей профиля пользователя")
    void updateUser_shouldUpdateAllFields_whenAllFieldsProvided() {
        // Arrange: Создаём аутентификацию и полное DTO обновления
        Authentication authentication = createAuthenticationForUser(testUser, "password123");

        UpdateUser updateUser = new UpdateUser();
        updateUser.setFirstName("Пётр");
        updateUser.setLastName("Петров");
        updateUser.setPhone("+7 (888) 777-66-55");

        // Act: Вызываем метод обновления профиля
        UpdateUser result = userService.updateUser(authentication, updateUser);

        // Assert: Проверяем возвращаемый DTO
        assertEquals("Пётр", result.getFirstName(), "Имя должно быть обновлено");
        assertEquals("Петров", result.getLastName(), "Фамилия должна быть обновлена");
        assertEquals("+7 (888) 777-66-55", result.getPhone(), "Телефон должен быть обновлён");

        // Assert: Проверяем фактическое состояние в БД
        UserEntity updatedUser = userRepository.findById(savedUserId)
                .orElseThrow(() -> new AssertionError("Пользователь должен существовать в БД"));

        assertEquals("Пётр", updatedUser.getFirstName(), "Имя должно быть обновлено в БД");
        assertEquals("Петров", updatedUser.getLastName(), "Фамилия должна быть обновлена в БД");
        assertEquals("+7 (888) 777-66-55", updatedUser.getPhone(), "Телефон должен быть обновлён в БД");
        assertEquals(testUser.getEmail(), updatedUser.getEmail(), "Email не должен изменяться");
        assertEquals(testUser.getRole(), updatedUser.getRole(), "Роль не должна изменяться");
    }

    /**
     * Создаёт объект {@link Authentication} для тестового пользователя.
     * <p>
     * <b>Вспомогательный метод</b> для единообразного создания аутентификации
     * во всех тестах. Имитирует успешную аутентификацию через Spring Security.
     *
     * @param userEntity сущность пользователя, для которого создаётся аутентификация
     * @param rawPassword сырой пароль для создания учётных данных (не используется для проверки)
     * @return объект {@link Authentication} с установленными principal и authorities
     */
    private Authentication createAuthenticationForUser(UserEntity userEntity, String rawPassword) {
        return new UsernamePasswordAuthenticationToken(
                userEntity.getEmail(), // principal (username)
                rawPassword, // credentials (не проверяется в тестах сервиса)
                List.of(new SimpleGrantedAuthority("ROLE_" + userEntity.getRole().name()))
        );
    }
}
