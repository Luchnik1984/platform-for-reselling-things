package ru.skypro.homework.service.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import ru.skypro.homework.dto.user.NewPassword;
import ru.skypro.homework.dto.user.UpdateUser;
import ru.skypro.homework.dto.user.User;
import ru.skypro.homework.entity.UserEntity;
import ru.skypro.homework.enums.Role;
import ru.skypro.homework.exceptions.InvalidPasswordException;
import ru.skypro.homework.mapper.UserMapper;
import ru.skypro.homework.repository.UserRepository;
import ru.skypro.homework.service.impl.UserServiceImpl;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit-тесты для {@link UserServiceImpl}.
 * Тестирует бизнес-логику сервиса работы с пользователями.
 *
 * <p>Использует Mockito для изоляции тестируемого сервиса от зависимостей.
 */
@ExtendWith(MockitoExtension.class)
public class UserServiceUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserServiceImpl userService;

    /* Тестовые константы */
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_WRONG_PASSWORD = "wrongPassword";
    private static final String TEST_NEW_PASSWORD = "newPassword123";
    private static final Integer TEST_USER_ID = 1;

    /* Тестовые данные (не статические!) */
    private String testHashedPassword;
    private String testNewHashedPassword;

    /**
     * Подготовка тестовых данных перед каждым тестом.
     * Генерирует реальные BCrypt хеши для реалистичного тестирования.
     * Cost factor 10 для скорости тестов
     */
    @BeforeEach
    void setUp() {

        BCryptPasswordEncoder realEncoder = new BCryptPasswordEncoder(10);
        testHashedPassword = realEncoder.encode(TEST_PASSWORD);
        testNewHashedPassword = realEncoder.encode(TEST_NEW_PASSWORD);
    }

    /**
     * Тестирует успешное получение информации о текущем пользователе.
     * Пользователь существует в системе.
     */
    @Test
    void getCurrentUser_UserExists_ReturnsUserDto() {

        UserEntity userEntity = createTestUserEntity();
        User expectedUser = createTestUserDto();

        when(authentication.getName()).thenReturn(TEST_EMAIL);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(userEntity));
        when(userMapper.toDto(userEntity)).thenReturn(expectedUser);

        User result = userService.getCurrentUser(authentication);

        assertThat(result).isEqualTo(expectedUser);
        verify(authentication, times(2)).getName();
        verify(userRepository).findByEmail(TEST_EMAIL);
        verify(userMapper).toDto(userEntity);
    }

    /**
     * Тестирует сценарий, когда аутентифицированный пользователь не найден в БД.
     * Это крайний случай нарушения целостности данных.
     * Должен выбросить NoSuchElementException.
     */
    @Test
    void getCurrentUser_UserNotFound_ThrowsResponseStatusException() {

        when(authentication.getName()).thenReturn(TEST_EMAIL);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getCurrentUser(authentication))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.UNAUTHORIZED)
                .hasMessageContaining("Несоответствие аутентификационных данных. " +
                        "Пожалуйста, войдите в систему еще раз");

        verify(authentication,times(2)).getName();
        verify(userRepository).findByEmail(TEST_EMAIL);
        verify(userMapper, never()).toDto(any());
    }

    /**
     * Тестирует успешное обновление профиля пользователя.
     * Обновляются только переданные поля.
     */
    @Test
    void updateUser_ValidData_UpdatesAndReturnsUser() {

        UserEntity userEntity = createTestUserEntity();
        UpdateUser updateUser = new UpdateUser();
        updateUser.setFirstName("НовоеИмя");
        updateUser.setLastName("НоваяФамилия");
        updateUser.setPhone("+7 (999) 111-22-33");

        when(authentication.getName()).thenReturn(TEST_EMAIL);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(userEntity));
        when(userRepository.save(userEntity)).thenReturn(userEntity);


        UpdateUser result = userService.updateUser(authentication, updateUser);

        assertThat(result).isEqualTo(updateUser);
        verify(authentication, times(2)).getName();
        verify(userRepository).findByEmail(TEST_EMAIL);
        verify(userMapper).updateEntityFromUpdateUser(updateUser, userEntity);
        verify(userRepository).save(userEntity);
    }

    /**
     * Тестирует частичное обновление профиля - только имя.
     */
    @Test
    void updateUser_PartialUpdateFirstNameOnly_UpdatesOnlyFirstName() {

        UserEntity userEntity = createTestUserEntity();
        UpdateUser updateUser = new UpdateUser();
        updateUser.setFirstName("ТолькоИмя"); /* Только имя, остальные поля null */

        when(authentication.getName()).thenReturn(TEST_EMAIL);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(userEntity));
        when(userRepository.save(userEntity)).thenReturn(userEntity);

        UpdateUser result = userService.updateUser(authentication, updateUser);

        assertThat(result).isEqualTo(updateUser);
        verify(userMapper).updateEntityFromUpdateUser(updateUser, userEntity);

        verify(userMapper).updateEntityFromUpdateUser(
                argThat(dto ->
                        dto.getFirstName().equals("ТолькоИмя") &&
                                dto.getLastName() == null &&
                                dto.getPhone() == null
                ),
                eq(userEntity)
        );
    }

    /**
     * Тестирует успешную смену пароля.
     * Текущий пароль верен, новый пароль хешируется и сохраняется.
     */
    @Test
    void updatePassword_ValidCurrentPassword_UpdatesPassword() {

        UserEntity userEntity = createTestUserEntity();
        NewPassword newPassword = new NewPassword();
        newPassword.setCurrentPassword(TEST_PASSWORD);
        newPassword.setNewPassword(TEST_NEW_PASSWORD);

        when(authentication.getName()).thenReturn(TEST_EMAIL);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(userEntity));

        /* Мокаем passwordEncoder для возврата true при проверке */
        when(passwordEncoder.matches(TEST_PASSWORD, testHashedPassword)).thenReturn(true);
        /* Мокаем passwordEncoder для возврата нового хеша */
        when(passwordEncoder.encode(TEST_NEW_PASSWORD)).thenReturn(testNewHashedPassword);

        when(userRepository.save(userEntity)).thenReturn(userEntity);

        userService.updatePassword(authentication, newPassword);

        verify(authentication, times(2)).getName();
        verify(userRepository).findByEmail(TEST_EMAIL);
        verify(passwordEncoder).matches(TEST_PASSWORD, testHashedPassword);
        verify(passwordEncoder).encode(TEST_NEW_PASSWORD);
        verify(userRepository).save(userEntity);

        assertThat(userEntity.getPassword()).isEqualTo(testNewHashedPassword);
    }

    /**
     * Тестирует сценарий, когда текущий пароль указан неверно.
     * Должен выбросить InvalidPasswordException.
     */
    @Test
    void updatePassword_InvalidCurrentPassword_ThrowsInvalidPasswordException() {

        UserEntity userEntity = createTestUserEntity();
        NewPassword newPassword = new NewPassword();
        newPassword.setCurrentPassword(TEST_WRONG_PASSWORD);
        newPassword.setNewPassword(TEST_NEW_PASSWORD);

        when(authentication.getName()).thenReturn(TEST_EMAIL);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(userEntity));
        /* Мокаем passwordEncoder для возврата false (неверный пароль) */
        when(passwordEncoder.matches(TEST_WRONG_PASSWORD, testHashedPassword)).thenReturn(false);

        assertThatThrownBy(() -> userService.updatePassword(authentication, newPassword))
                .isInstanceOf(InvalidPasswordException.class)
                .hasMessage("Неверный пароль");

        verify(authentication, times(2)).getName();
        verify(userRepository).findByEmail(TEST_EMAIL);
        verify(passwordEncoder).matches(TEST_WRONG_PASSWORD, testHashedPassword);
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    /**
     * Создает тестовый объект UserEntity.
     * Использует сгенерированный в @BeforeEach хеш пароля.
     *
     * @return тестовый UserEntity
     */
    private UserEntity createTestUserEntity() {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(TEST_USER_ID);
        userEntity.setEmail(TEST_EMAIL);
        userEntity.setPassword(testHashedPassword);
        userEntity.setFirstName("Иван");
        userEntity.setLastName("Иванов");
        userEntity.setPhone("+7 (999) 123-45-67");
        userEntity.setRole(Role.USER);
        userEntity.setEnabled(true);
        return userEntity;
    }

    /**
     * Создает тестовый объект User DTO.
     *
     * @return тестовый User DTO
     */
    private User createTestUserDto() {
        User user = new User();
        user.setId(TEST_USER_ID);
        user.setEmail(TEST_EMAIL);
        user.setFirstName("Иван");
        user.setLastName("Иванов");
        user.setPhone("+7 (999) 123-45-67");
        user.setRole(Role.USER);
        user.setImage("/images/users/1-avatar.jpg");
        return user;
    }
}
