package ru.skypro.homework.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.test.context.ActiveProfiles;
import ru.skypro.homework.dto.reg.Register;
import ru.skypro.homework.dto.user.UpdateUser;
import ru.skypro.homework.dto.user.User;
import ru.skypro.homework.entity.ImageEntity;
import ru.skypro.homework.entity.UserEntity;
import ru.skypro.homework.enums.Role;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit-тесты для {@link UserMapper}.
 * Проверяет корректность маппинга между UserEntity и DTO.
 * <p>
 * Тесты проверяют:
 * <ul>
 *   <li>Правильность маппинга полей</li>
 *   <li>Обработку null значений</li>
 *   <li>Установку значений по умолчанию</li>
 *   <li>Частичное обновление только не-null полей</li>
 * </ul>
 *
 * @see UserMapper
 * @see UserMapperImpl
 */
@Tag("unit")
@ActiveProfiles("test")
@DisplayName("UserMapper impl tests")
public class UserMapperUnitTest {

    /** Экземпляр тестируемого маппера, полученный через MapStruct */
    private UserMapper mapper;

    /** Тестовый email для создания объектов */
    private static final String TEST_EMAIL = "test@example.com";
    /** Тестовое имя пользователя */
    private static final String TEST_FIRST_NAME = "Иван";
    /** Тестовое имя пользователя (новое для обновлений) */
    private static final String NEW_FIRST_NAME = "Петр";
    /** Тестовая фамилия пользователя */
    private static final String TEST_LAST_NAME = "Иванов";
    /** Тестовая фамилия пользователя (новая для обновлений) */
    private static final String NEW_LAST_NAME = "Петров";
    /** Тестовый телефон */
    private static final String TEST_PHONE = "+7 (999) 123-45-67";
    /** Новый телефон для обновлений */
    private static final String NEW_PHONE = "+7 (888) 888-88-88";
    /** Тестовый пароль (для Register DTO) */
    private static final String TEST_PASSWORD = "password123";

    /**
     * Инициализация перед каждым тестом.
     * Создаёт экземпляр маппера через MapStruct.
     */
    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(UserMapper.class);
    }

    /**
     * Создаёт тестовый объект {@link Register} с заполненными полями.
     * Используется для тестирования маппинга Register -> UserEntity.
     *
     * @param username email пользователя (будет использован как username)
     * @param role     роль пользователя, может быть null
     * @return заполненный объект Register
     */
    private Register createTestRegister(String username, Role role) {
        Register register = new Register();
        register.setUsername(username);
        register.setPassword(TEST_PASSWORD);
        register.setFirstName(TEST_FIRST_NAME);
        register.setLastName(TEST_LAST_NAME);
        register.setPhone(TEST_PHONE);
        register.setRole(role);
        return register;
    }

    /**
     * Создаёт тестовый объект {@link UserEntity} с заполненными полями.
     * Используется для тестирования маппинга UserEntity -> User.
     *
     * @param id    идентификатор пользователя
     * @param email email пользователя
     * @param role  роль пользователя
     * @param image изображение пользователя, может быть null
     * @return заполненный объект UserEntity
     */
    private UserEntity createTestUserEntity(Integer id, String email, Role role, ImageEntity image) {
        UserEntity entity = new UserEntity();
        entity.setId(id);
        entity.setEmail(email);
        entity.setFirstName(TEST_FIRST_NAME);
        entity.setLastName(TEST_LAST_NAME);
        entity.setPhone(TEST_PHONE);
        entity.setRole(role);
        entity.setEnabled(true);
        entity.setImage(image);
        return entity;
    }

    /**
     * Создаёт тестовый объект {@link UpdateUser} с заполненными полями.
     * Используется для тестирования обновления профиля.
     *
     * @param firstName новое имя, может быть null
     * @param lastName  новая фамилия, может быть null
     * @param phone     новый телефон, может быть null
     * @return заполненный объект UpdateUser
     */
    private UpdateUser createTestUpdateUser(String firstName, String lastName, String phone) {
        UpdateUser updateUser = new UpdateUser();
        updateUser.setFirstName(firstName);
        updateUser.setLastName(lastName);
        updateUser.setPhone(phone);
        return updateUser;
    }

    /**
     * Создаёт тестовый объект {@link ImageEntity}.
     * Используется для тестирования маппинга изображений.
     *
     * @param filePath путь к файлу изображения
     * @return заполненный объект ImageEntity
     */
    private ImageEntity createTestImage(String filePath) {
        ImageEntity image = new ImageEntity();
        image.setFilePath(filePath);
        image.setFileSize(2048L);
        image.setMediaType("image/jpeg");
        return image;
    }

    /**
     * Тест: преобразование {@link Register} -> {@link UserEntity} с полными данными.
     * <p>
     * Проверяет:
     * <ul>
     *   <li>Все поля корректно маппятся</li>
     *   <li>Email устанавливается из username</li>
     *   <li>Роль сохраняется</li>
     *   <li>Пользователь enabled по умолчанию</li>
     *   <li>Пароль не устанавливается (должен хешироваться отдельно)</li>
     * </ul>
     */
    @Test
    @DisplayName("toEntity: Register -> UserEntity с полными данными")
    void toEntity_ShouldMapFullRegisterToUserEntity() {

        Register register = createTestRegister(TEST_EMAIL, Role.USER);

        UserEntity entity = mapper.toEntity(register);

        assertNotNull(entity, "Созданная сущность не должна быть null");
        assertEquals(TEST_EMAIL, entity.getEmail(), "Email должен совпадать с username");
        assertEquals(TEST_FIRST_NAME, entity.getFirstName(), "Имя должно корректно маппиться");
        assertEquals(TEST_LAST_NAME, entity.getLastName(), "Фамилия должна корректно маппиться");
        assertEquals(TEST_PHONE, entity.getPhone(), "Телефон должен корректно маппиться");
        assertEquals(Role.USER, entity.getRole(), "Роль должна сохраняться");
        assertTrue(entity.isEnabled(), "Пользователь должен быть enabled по умолчанию");
        assertNull(entity.getPassword(), "Пароль должен быть null (хешируется отдельно)");
        assertNull(entity.getImage(), "Изображение должно быть null по умолчанию");
        assertNull(entity.getId(), "ID должен быть null (генерируется БД)");
    }

    /**
     * Тест: преобразование {@link Register} -> {@link UserEntity} без указания роли.
     * <p>
     * Проверяет, что при null роли устанавливается роль по умолчанию {@link Role#USER}.
     */
    @Test
    @DisplayName("toEntity: Register без роли -> роль по умолчанию USER")
    void toEntity_ShouldSetDefaultRoleWhenNull() {

        Register register = createTestRegister(TEST_EMAIL, null);

        UserEntity entity = mapper.toEntity(register);

        assertEquals(Role.USER, entity.getRole(),
                "При отсутствии роли должна устанавливаться роль USER по умолчанию");
    }

    /**
     * Тест: преобразование {@link Register} -> {@link UserEntity} с ролью ADMIN.
     * <p>
     * Проверяет сохранение роли ADMIN при маппинге.
     */
    @Test
    @DisplayName("toEntity: Register с ролью ADMIN -> роль ADMIN")
    void toEntity_ShouldPreserveAdminRole() {

        Register register = createTestRegister("admin@example.com", Role.ADMIN);

        UserEntity entity = mapper.toEntity(register);

        assertEquals(Role.ADMIN, entity.getRole(),
                "Роль ADMIN должна сохраняться при маппинге");
    }

    /**
     * Тест: преобразование {@link UserEntity} -> {@link User} с изображением.
     * <p>
     * Проверяет:
     * <ul>
     *   <li>Все поля корректно маппятся</li>
     *   <li>Ссылка на изображение генерируется через {@link ImageEntity#getImageUrl()}</li>
     * </ul>
     */
    @Test
    @DisplayName("toDto: UserEntity с изображением -> User с URL изображения")
    void toDto_ShouldMapUserEntityWithImageToUser() {

        ImageEntity image = createTestImage("uploads/users/123-avatar.jpg");
        UserEntity entity = createTestUserEntity(1, TEST_EMAIL, Role.USER, image);

        User dto = mapper.toDto(entity);

        assertNotNull(dto, "DTO не должен быть null");
        assertEquals(1, dto.getId(), "ID должен корректно маппиться");
        assertEquals(TEST_EMAIL, dto.getEmail(), "Email должен корректно маппиться");
        assertEquals(TEST_FIRST_NAME, dto.getFirstName(), "Имя должно корректно маппиться");
        assertEquals(TEST_LAST_NAME, dto.getLastName(), "Фамилия должна корректно маппиться");
        assertEquals(TEST_PHONE, dto.getPhone(), "Телефон должен корректно маппиться");
        assertEquals(Role.USER, dto.getRole(), "Роль должна корректно маппиться");
        assertEquals(image.getImageUrl(), dto.getImage(),
                "URL изображения должен генерироваться через getImageUrl()");
    }

    /**
     * Тест: преобразование {@link UserEntity} -> {@link User} без изображения.
     * <p>
     * Проверяет, что при отсутствии изображения поле image устанавливается в null.
     */
    @Test
    @DisplayName("toDto: UserEntity без изображения -> image = null")
    void toDto_ShouldHandleNullImage() {

        UserEntity entity = createTestUserEntity(2, TEST_EMAIL, Role.USER, null);

        User dto = mapper.toDto(entity);

        assertNotNull(dto, "DTO не должен быть null");
        assertEquals(2, dto.getId(), "ID должен корректно маппиться");
        assertEquals(TEST_FIRST_NAME, dto.getFirstName(), "Имя должно корректно маппиться");
        assertNull(dto.getImage(), "При отсутствии изображения поле должно быть null");
    }

    /**
     * Тест: обновление {@link UserEntity} из {@link UpdateUser} (частичное обновление).
     * <p>
     * Проверяет:
     * <ul>
     *   <li>Обновляются только не-null поля</li>
     *   <li>Роль не изменяется (не входит в UpdateUser)</li>
     *   <li>null поля игнорируются</li>
     * </ul>
     */
    @Test
    @DisplayName("updateEntityFromUpdateUser: обновление только не-null полей")
    void updateEntityFromUpdateUser_ShouldUpdateOnlyNonNullFields() {

        UserEntity existingEntity = createTestUserEntity(1, TEST_EMAIL, Role.USER, null);
        UpdateUser updateUser = createTestUpdateUser(NEW_FIRST_NAME, null, NEW_PHONE);

        mapper.updateEntityFromUpdateUser(updateUser, existingEntity);

        assertEquals(NEW_FIRST_NAME, existingEntity.getFirstName(),
                "Имя должно обновиться при не-null значении");
        assertEquals(TEST_LAST_NAME, existingEntity.getLastName(),
                "Фамилия не должна измениться при null значении");
        assertEquals(NEW_PHONE, existingEntity.getPhone(),
                "Телефон должен обновиться при не-null значении");
        assertEquals(Role.USER, existingEntity.getRole(),
                "Роль не должна изменяться через UpdateUser");
    }

    /**
     * Тест: передача null {@link UpdateUser} при обновлении.
     * <p>
     * Проверяет, что при null DTO сущность не изменяется.
     */
    @Test
    @DisplayName("updateEntityFromUpdateUser: null DTO -> никаких изменений")
    void updateEntityFromUpdateUser_ShouldDoNothingForNullDto() {

        UserEntity existingEntity = createTestUserEntity(1, TEST_EMAIL, Role.USER, null);

        mapper.updateEntityFromUpdateUser(null, existingEntity);

        assertEquals(TEST_FIRST_NAME, existingEntity.getFirstName(),
                "Имя не должно измениться при null DTO");
        assertEquals(TEST_LAST_NAME, existingEntity.getLastName(),
                "Фамилия не должна измениться при null DTO");
        assertEquals(TEST_PHONE, existingEntity.getPhone(),
                "Телефон не должен измениться при null DTO");
    }

    /**
     * Тест: обновление {@link UserEntity} из {@link Register} с изменением роли.
     * <p>
     * Проверяет обновление полей и роли при использовании Register DTO.
     */
    @Test
    @DisplayName("updateEntityFromRegister: обновление полей и роли")
    void updateEntityFromRegister_ShouldUpdateFieldsAndRole() {

        UserEntity existingEntity = createTestUserEntity(1, TEST_EMAIL, Role.USER, null);
        Register updateDto = createTestRegister("new@example.com", Role.ADMIN);
        updateDto.setFirstName(NEW_FIRST_NAME);
        updateDto.setLastName(null); // Явно null - не обновлять
        updateDto.setPhone(NEW_PHONE);

        mapper.updateEntityFromRegister(updateDto, existingEntity);

        assertEquals(NEW_FIRST_NAME, existingEntity.getFirstName(),
                "Имя должно обновиться");
        assertEquals(TEST_LAST_NAME, existingEntity.getLastName(),
                "Фамилия не должна измениться при null значении");
        assertEquals(NEW_PHONE, existingEntity.getPhone(),
                "Телефон должен обновиться");
        assertEquals(Role.ADMIN, existingEntity.getRole(),
                "Роль должна обновиться на ADMIN");
    }

    /**
     * Тест: обновление {@link UserEntity} из {@link Register} с null ролью.
     * <p>
     * Проверяет, что при null роли в DTO существующая роль не изменяется.
     */
    @Test
    @DisplayName("updateEntityFromRegister: роль null -> роль не меняется (остаётся старая)")
    void updateEntityFromRegister_ShouldNotUpdateRoleWhenNull() {

        UserEntity existingEntity = createTestUserEntity(1, TEST_EMAIL, Role.USER, null);
        Register updateDto = createTestRegister(TEST_EMAIL, null);
        updateDto.setFirstName(NEW_FIRST_NAME);

        mapper.updateEntityFromRegister(updateDto, existingEntity);

        assertEquals(NEW_FIRST_NAME, existingEntity.getFirstName(),
                "Имя должно обновиться");
        assertEquals(Role.USER, existingEntity.getRole(),
                "Роль не должна измениться при null значении в DTO");
    }

    /**
     * Тест: проверка null-safety для метода {@link UserMapper#toDto(UserEntity)}.
     * <p>
     * Проверяет, что при передаче null возвращается null.
     */
    @Test
    @DisplayName("null safety: toDto с null entity → null")
    void toDto_ShouldReturnNullForNullInput() {
        assertNull(mapper.toDto(null),
                "Метод toDto должен возвращать null при null входном значении");
    }

    /**
     * Тест: проверка null-safety для метода {@link UserMapper#toEntity(Register)}.
     * <p>
     * Проверяет, что при передаче null возвращается null.
     */
    @Test
    @DisplayName("null safety: toEntity с null register → null")
    void toEntity_ShouldReturnNullForNullInput() {
        assertNull(mapper.toEntity(null),
                "Метод toEntity должен возвращать null при null входном значении");
    }

    /**
     * Тест: проверка метода ensureRoleNotNull через {@link UserMapper#toEntity(Register)}.
     * <p>
     * Проверяет, что при null роли в Register устанавливается роль {@link Role#USER}.
     */
    @Test
    @DisplayName("Проверка ensureRoleNotNull: устанавливает USER если роль null")
    void ensureRoleNotNull_ShouldSetUserRoleWhenNull() {

        Register register = createTestRegister(TEST_EMAIL, null);

        UserEntity result = mapper.toEntity(register);

        assertEquals(Role.USER, result.getRole(),
                "При null роли должна устанавливаться роль USER по умолчанию");
    }

    /**
     * Тест: проверка маппинга пути изображения с начальным слэшем.
     * <p>
     * Проверяет корректную обработку путей, начинающихся с "/".
     */
    @Test
    @DisplayName("Проверка маппинга изображения: путь с начальным слэшем")
    void imageMapping_ShouldHandlePathWithLeadingSlash() {

        ImageEntity image = createTestImage("/uploads/avatars/user-1.jpg");
        UserEntity entity = createTestUserEntity(1, TEST_EMAIL, Role.USER, image);

        User dto = mapper.toDto(entity);

        assertEquals("/images/uploads/avatars/user-1.jpg", dto.getImage(),
                "Путь с начальным слэшем должен корректно обрабатываться");
    }

}
