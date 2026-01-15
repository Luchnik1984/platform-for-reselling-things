package ru.skypro.homework.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.user.NewPassword;
import ru.skypro.homework.dto.user.UpdateUser;
import ru.skypro.homework.dto.user.User;
import ru.skypro.homework.entity.UserEntity;
import ru.skypro.homework.repository.UserRepository;
import ru.skypro.homework.service.ImageService;
import ru.skypro.homework.service.UserService;
import org.springframework.security.core.Authentication;
import ru.skypro.homework.util.SecurityUtils;

import javax.validation.Valid;


/**
 * Контроллер для работы с аккаунтами пользователей.
 * Предоставляет набор CRUD-операций
 * <p>Все эндпоинты требуют аутентификации пользователя.
 * Пользователь может управлять только своим профилем.
 *
 * @see UserService сервис, реализующий бизнес-логику работы с пользователями
 */
@Slf4j
@CrossOrigin(value = "http://localhost:3000")
@RestController
@RequiredArgsConstructor
@Tag(name = "Пользователи", description = "API для работы с аккаунтами пользователей")
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final ImageService imageService;

    /**
     * Обновление пароля авторизованного пользователя
     *
     * <p>Требует указания текущего пароля для подтверждения личности.
     * Новый пароль должен соответствовать требованиям валидации.
     * @param newPassword DTO с текущим и новым паролями
     * @param authentication объект аутентификации Spring Security
     * @throws ru.skypro.homework.exceptions.InvalidPasswordException если текущий пароль неверен
     */
    @Operation(
            summary = "Обновление пароля",
            description = "Обновляет пароль пользователя" +
                    "Требует указания текущего пароля для подтверждения."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK: Пароль успешно обновлен"),
            @ApiResponse(responseCode = "401", description = "Unauthorized: Требуется авторизация",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden: У Вас нет прав сменить пароль",
                    content = @Content(schema = @Schema(hidden = true)))
    })

    @PostMapping("/set_password")
    @ResponseStatus(HttpStatus.OK)
    public void setPassword(@Valid @RequestBody NewPassword newPassword,
                            Authentication authentication) {
        log.info("Запрос на смену пароля от пользователя: {}", authentication.getName());

        userService.updatePassword(authentication, newPassword);

        log.debug("Пароль пользователя {} успешно обновлен", authentication.getName());
    }

    /**
     * Эндпоинт на получение информации о текущем аутентифицированном пользователе.
     * <p>Возвращает полную информацию о профиле пользователя:
     * ID, email, имя, фамилию, телефон, роль и ссылку на аватар.
     * Email пользователя соответствует значению, полученному из аутентификации.
     *
     * @param authentication объект аутентификации Spring Security
     * @return DTO с информацией о пользователе
     */
    @Operation(
            summary = "Получение информации об авторизованном пользователе",
            description = "Получает ID, логин, имя, фамилия, телефон, роль, ссылка на аватар пользователя"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "OK",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "401",
                    description = "Unauthorized: Требуется авторизация",
                    content = @Content(schema = @Schema(hidden = true))),
    })
    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    public User getUser(Authentication authentication) {

        log.info("Запрос информации о пользователе: {}", authentication.getName());

        User user = userService.getCurrentUser(authentication);

        log.debug("Информация о пользователе {} успешно получена", authentication.getName());

        return user;
    }

    /**
     * Обновление информации об авторизованном пользователя.
     *
     * <p>Позволяет частично обновить информацию о пользователе:
     * имя, фамилию и телефон. Email и роль не могут быть изменены через этот эндпоинт.
     *
     * @param updateUser DTO с обновленными данными профиля
     * @param authentication объект аутентификации Spring Security
     * (теоретически возможно при рассинхронизации данных).
     * @return DTO с обновленными данными (тот же объект, что был передан).
     */
    @Operation(
            summary = "Обновление информации об авторизованном пользователе",
            description = "Обновляет имя, фамилия, телефон пользователя"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "OK: данные обновлены",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UpdateUser.class))),
            @ApiResponse(responseCode = "401",
                    description = "Unauthorized: Требуется авторизация",
                    content = @Content(schema = @Schema(hidden = true))),
    })
    @PatchMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    public UpdateUser updateUser(@Valid @RequestBody UpdateUser updateUser,
                                 Authentication authentication) {
        log.info("Запрос на обновление профиля пользователя: {}", authentication.getName());
        log.debug("Новые данные: firstName={}, lastName={}, phone={}",
                updateUser.getFirstName(), updateUser.getLastName(), updateUser.getPhone());

        UpdateUser result = userService.updateUser(authentication, updateUser);

        log.info("Профиль пользователя {} успешно обновлен", authentication.getName());
        return result;
    }

    /**
     * Обновление аватара авторизованного пользователя
     *
     * <p>Загружает новый файл аватара и сохраняет его в файловой системе.
     * Поддерживает изображения форматов JPEG, PNG, GIF.
     * Максимальный размер файла: 10MB.
     *
     * <p>!!! <strong>Внимание:</strong> На текущем этапе это заглушка.
     * Полная реализация будет добавлена после реализации ImageService.
     * @param image : файл аватара.
     * @param authentication объект аутентификации Spring Security.
     */
    @Operation(
            summary = "Обновление аватара авторизованного пользователя",
            description = "Обновляет файл аватара на внутреннем хранилище для пользователя. " +
                    "Поддерживает JPEG, PNG, GIF"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "OK: Аватар пользователя обновлен"),
            @ApiResponse(responseCode = "401",
                    description = "Unauthorized: Требуется авторизация",
                    content = @Content(schema = @Schema(hidden = true))),
    })

    @PatchMapping(value ="/me/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public void  updateUserImage(@RequestPart("image") MultipartFile image,
                                                Authentication authentication) {

        String email = authentication.getName();
        log.info("Запрос на обновление аватара пользователя: {}", email);
        // 1. Проверка файла (внутренняя валидация)
        validateImageFile(image);

        // 2. Получение пользователя с проверкой существования
        UserEntity user = SecurityUtils.getAuthenticatedUser(userRepository, authentication);
        log.debug("Пользователь найден: {} {} (ID: {})",
                user.getFirstName(), user.getLastName(), user.getId());

        // 3. Загрузка изображения
        try {
            log.debug("Файл: {} ({} байт, тип: {})",
                    image.getOriginalFilename(),
                    image.getSize(),
                    image.getContentType());

            imageService.uploadUserImage(user.getId(), image);
            log.info("Аватар пользователя {} успешно обновлен ", email);
        } catch (Exception e) {
            log.error("Ошибка при обновлении аватара пользователя {}: {}", email, e.getMessage(), e);
            // Все ошибки, кроме 401, будут обработаны GlobalExceptionHandler как 500
            throw new RuntimeException("Не удалось обновить аватар", e);
        }
    }

    /**
     * Внутренняя валидация файла изображения.
     * В случае ошибки выбрасывает RuntimeException, который будет обработан
     * GlobalExceptionHandler и вернет 500 Internal Server Error.
     *
     * @param image файл для проверки
     * @throws RuntimeException если файл не проходит валидацию
     */
    private void validateImageFile(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new RuntimeException("Файл изображения не может быть пустым");
        }

        // Проверка размера (5MB)
        long maxSize = 5 * 1024 * 1024;
        if (image.getSize() > maxSize) {
            throw new RuntimeException(
                    String.format("Размер файла превышает максимально допустимый (%d MB)",
                            maxSize / (1024 * 1024)));
        }

        // Проверка типа файла
        String contentType = image.getContentType();
        if (contentType == null ||
                !(contentType.equals("image/jpeg") ||
                        contentType.equals("image/png") ||
                        contentType.equals("image/gif") ||
                        contentType.equals("image/webp"))) {
            throw new RuntimeException(
                    "Недопустимый тип файла. Допустимые типы: JPEG, PNG, GIF, WebP");
        }

        log.debug("Файл прошел валидацию: {}, размер: {} байт, тип: {}",
                image.getOriginalFilename(), image.getSize(), contentType);
    }
}
