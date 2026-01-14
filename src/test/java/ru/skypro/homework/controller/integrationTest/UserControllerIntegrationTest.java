package ru.skypro.homework.controller.integrationTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import ru.skypro.homework.AbstractIntegrationTest;
import ru.skypro.homework.dto.user.NewPassword;
import ru.skypro.homework.dto.user.UpdateUser;
import ru.skypro.homework.enums.Role;
import ru.skypro.homework.repository.UserRepository;
import ru.skypro.homework.entity.UserEntity;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Интеграционные тесты {@code UserController} с {@link MockMvc}.
 * <p>
 * Покрывает эндпоинты:
 * <ul>
 *   <li>GET /users/me</li>
 *   <li>PATCH /users/me</li>
 *   <li>POST /users/set_password</li>
 * </ul>
 */

class UserControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private String generateUniqueEmail(String baseName) {
        long suffix = Math.abs(System.nanoTime() % 1_000_000);
        return baseName + suffix + "@m.ru";
    }

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    private UserEntity createUser(String email, Role role) {
        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("password123"));
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPhone("+7 999 123-45-67");
        user.setRole(role);
        user.setEnabled(true);
        return userRepository.save(user);
    }

    @Test
    @DisplayName("GET /users/me без авторизации -> 401")
    void shouldReturn401_whenGetMeWithoutAuth() throws Exception {
        mockMvc.perform(get("/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /users/me с ролью USER -> 200 и корректная структура User DTO")
    @WithMockUser(username = "user@test.ru", roles = "USER")
    void shouldReturnUserDto_whenGetMeAsUser() throws Exception {
        createUser("user@test.ru", Role.USER);

        mockMvc.perform(get("/users/me"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.email", is("user@test.ru")))
                .andExpect(jsonPath("$.firstName", notNullValue()))
                .andExpect(jsonPath("$.lastName", notNullValue()))
                .andExpect(jsonPath("$.phone", notNullValue()))
                .andExpect(jsonPath("$.role", is("USER")));
    }

    @Test
    @DisplayName("PATCH /users/me с ролью USER -> 200 и обновленные поля")
    @WithMockUser(username = "patch@test.ru", roles = "USER")
    void shouldUpdateUser_whenPatchMeAsUser() throws Exception {
        createUser("patch@test.ru", Role.USER);

        UpdateUser request = new UpdateUser();
        request.setFirstName("Petr");
        request.setLastName("Petrov");
        request.setPhone("+7 999 111-11-11");

        mockMvc.perform(patch("/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.firstName", is("Petr")))
                .andExpect(jsonPath("$.lastName", is("Petrov")))
                .andExpect(jsonPath("$.phone", is("+7 999 111-11-11")));
    }

    @Test
    @DisplayName("PATCH /users/me без авторизации -> 401")
    void shouldReturn401_whenPatchMeUnauthorized() throws Exception {
        UpdateUser request = new UpdateUser();
        request.setFirstName("A");
        request.setLastName("B");
        request.setPhone("7 999 111-11-11");

        mockMvc.perform(patch("/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }


    @Test
    @DisplayName("POST /users/set_password валидный currentPassword -> 200")
    @WithMockUser(username = "pass@test.ru", roles = "USER")
    void shouldUpdatePassword_whenCurrentPasswordValid() throws Exception {
        createUser("pass@test.ru", Role.USER);

        NewPassword request = new NewPassword();
        request.setCurrentPassword("password123");
        request.setNewPassword("newpassword123");

        mockMvc.perform(post("/users/set_password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /users/set_password неверный currentPassword -> 401")
    @WithMockUser(username = "wrongpass@test.ru", roles = "USER")
    void shouldReturn401_whenCurrentPasswordInvalid() throws Exception {
        createUser("wrongpass@test.ru", Role.USER);

        NewPassword request = new NewPassword();
        request.setCurrentPassword("wrongpass123");
        request.setNewPassword("newpassword123");

        mockMvc.perform(post("/users/set_password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PATCH /users/me/image без авторизации -> 401")
    void shouldReturn401_whenUpdateUserImageWithoutAuth() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "image", "avatar.png", "image/png", "fake-avatar".getBytes());

        mockMvc.perform(multipart("/users/me/image")
                        .file(image)
                        .with(request -> { request.setMethod("PATCH"); return request; }))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PATCH /users/me/image USER -> 200")
    @WithMockUser(username = "imguser@test.ru", roles = "USER")
    void shouldUpdateUserImage_whenUserAuthorized() throws Exception {
        createUser("imguser@test.ru", Role.USER);

        MockMultipartFile image = new MockMultipartFile(
                "image", "avatar.png", "image/png", "fake-avatar".getBytes());

        mockMvc.perform(multipart("/users/me/image")
                        .file(image)
                        .with(request -> { request.setMethod("PATCH"); return request; }))
                .andExpect(status().isOk());
    }

}