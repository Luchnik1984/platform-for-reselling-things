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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.user.NewPassword;
import ru.skypro.homework.dto.user.UpdateUser;
import ru.skypro.homework.dto.user.User;
import ru.skypro.homework.enums.Role;

import javax.validation.Valid;


/**
 * Контроллер для работы с аккаунтами пользователей.
 * Предоставляет набор CRUD-операций
 * На этапе 1 реализованы заглушки всех методов.
 */
@Slf4j
@CrossOrigin(value = "http://localhost:3000")
@RestController
@RequiredArgsConstructor
@Tag(name = "Пользователи", description = "API для работы с аккаунтами пользователей")
@RequestMapping("/users")
public class UserController {

    /**
     * Обновление пароля авторизованного пользователя
     *
     * @param newPassword DTO с текущим и новым паролями
     */
    @Operation(
            summary = "Обновление пароля",
            description = "Обновляет пароль пользователя"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK: Пароль успешно обновлен"),
            @ApiResponse(responseCode = "401", description = "Unauthorized: Требуется авторизация", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden: У Вас нет прав сменить пароль", content = @Content(schema = @Schema(hidden = true)))
    })

    @PostMapping("/set_password")
    @ResponseStatus(HttpStatus.OK)
    public void setPassword(@Valid @RequestBody NewPassword newPassword) {
        log.info("Был вызван метод контроллера setPassword");
    }

    /**
     * Эндпоинт на получение информации об авторизованном пользователе
     */
    @Operation(
            summary = "Получение информации об авторизованном пользователе",
            description = "Получает ID, логин, имя, фамилия, телефон, роль, ссылка на аватар пользователя"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized: Требуется авторизация", content = @Content(schema = @Schema(hidden = true))),
    })
    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    public User getUser() {

        log.info("Был вызван метод контроллера getUser");

        //создаем заглушку
        User user = new User();
        user.setId(1);
        user.setEmail("user@mail.com");
        user.setPhone("+7 (913) 472-34-23");
        user.setRole(Role.USER);
        user.setFirstName("Name");
        user.setLastName("LastName");
        user.setImage("/images/users/1-avatar.jpg");

        return user;
    }

    /**
     * Обновление информации об авторизованном пользователе
     *
     * @param updateUser : Имя Фамилия и номер телефона
     * @return DTO UpdateUser
     */
    @Operation(
            summary = "Обновление информации об авторизованном пользователе",
            description = "Обновляет имя, фамилия, телефон пользователя"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK: данные обновлены",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UpdateUser.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized: Требуется авторизация", content = @Content(schema = @Schema(hidden = true))),
    })
    @PatchMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    public UpdateUser updateUser(@Valid @RequestBody UpdateUser updateUser) {
        log.info("Был вызван метод контроллера updateUser");
        return updateUser;
    }

    /**
     * Обновление аватара авторизованного пользователя
     *
     * @param image : файл аватара
     */
    @Operation(
            summary = "Обновление аватара авторизованного пользователя",
            description = "Обновляет файл аватара на внутреннем хранилище для пользователя"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK: Аватар пользователя обновлен"),
            @ApiResponse(responseCode = "401", description = "Unauthorized: Требуется авторизация", content = @Content(schema = @Schema(hidden = true))),
    })
    @ResponseStatus(HttpStatus.OK)
    @PatchMapping("/me/image")
    public ResponseEntity<Void> updateUserImage(@RequestPart("image") MultipartFile image) {

        log.info("Был вызван метод контроллера updateUserImage. " +
                        "Имя файла: {}, размер: {} байт, тип: {}",
                image.getOriginalFilename(),
                image.getSize(),
                image.getContentType());
        log.debug("Заглушка: файл '{}' принят для обновления аватара",
                image.getOriginalFilename());

        return ResponseEntity.ok().build();
    }
}
