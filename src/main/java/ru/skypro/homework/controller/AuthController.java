package ru.skypro.homework.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.skypro.homework.dto.auth.Login;
import ru.skypro.homework.service.AuthService;

import javax.validation.Valid;

/**
 * Контроллер для авторизации пользователей.
 */
@Slf4j
@CrossOrigin(value = "http://localhost:3000")
@RestController
@RequiredArgsConstructor
@Tag(name = "Авторизация", description = "API для авторизации пользователей")
public class AuthController {

    private final AuthService authService;

    /**
     * Авторизация пользователя.
     * Basic Auth, но здесь проверяем логин/пароль из тела
     */
    @Operation(
            summary = "Авторизация пользователя",
            description = "Проверяет учетные данные и аутентифицирует пользователя"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешная аутентификация"),
            @ApiResponse(responseCode = "401", description = "Неверные учетные данные")
    })
    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public void login(@Valid @RequestBody Login login) {
        log.info("Авторизация пользователя: {}", login.getUsername());
        if (authService.login(login.getUsername(), login.getPassword())) {
            log.debug("Авторизация успешна");
        } else {
            log.warn("Неверные учетные данные для пользователя: {}", login.getUsername());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
    }
}
