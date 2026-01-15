package ru.skypro.homework.controller.integrationTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import ru.skypro.homework.AbstractIntegrationTest;
import ru.skypro.homework.dto.ads.CreateOrUpdateAd;
import ru.skypro.homework.entity.AdEntity;
import ru.skypro.homework.entity.UserEntity;
import ru.skypro.homework.enums.Role;
import ru.skypro.homework.repository.AdRepository;
import ru.skypro.homework.repository.CommentRepository;
import ru.skypro.homework.repository.UserRepository;

import java.io.IOException;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Интеграционные тесты {@code AdController} с {@link MockMvc}.
 * <p>
 * Покрывает основные эндпоинты /ads и проверки прав доступа:
 * <ul>
 *   <li>GET /ads (public)</li>
 *   <li>GET /ads/me (auth)</li>
 *   <li>POST /ads (USER/ADMIN)</li>
 *   <li>PATCH /ads/{id} (author or ADMIN)</li>
 *   <li>DELETE /ads/{id} (author or ADMIN)</li>
 * </ul>
 */

@Tag("integration")
@DisplayName("AdController Integration Tests")
class AdControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Autowired private AdRepository adRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private CommentRepository commentRepository;

    @Autowired private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        commentRepository.deleteAll();
        adRepository.deleteAll();
        userRepository.deleteAll();
    }

    private MockMultipartFile loadTestJpg() throws IOException {
        return new MockMultipartFile(
                "image",
                "test.jpg",
                "image/jpeg",
                new ClassPathResource("image/test.jpg").getInputStream().readAllBytes()
        );
    }

    private UserEntity createUser(String email, Role role) {
        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("password123"));
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPhone("+7 999 111-11-11");
        user.setRole(role);
        user.setEnabled(true);
        return userRepository.save(user);
    }

    private AdEntity createAd(UserEntity author) {
        AdEntity ad = new AdEntity();
        ad.setTitle("Valid Ad Title");
        ad.setDescription("This is a valid description with at least 8 characters");
        ad.setPrice(100);
        ad.setAuthor(author);
        return adRepository.save(ad);
    }

    @Test
    @DisplayName("GET /ads -> 200, структура Ads")
    void shouldReturnAds_whenPublicGetAll() throws Exception {
        mockMvc.perform(get("/ads"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count", notNullValue()))
                .andExpect(jsonPath("$.results", isA(java.util.List.class)));
    }

    @Test
    @DisplayName("GET /ads/me без auth -> 401")
    void shouldReturn401_whenGetAdsMeWithoutAuth() throws Exception {
        mockMvc.perform(get("/ads/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /ads/me с ролью USER -> 200")
    @WithMockUser(username = "me@test.ru", roles = "USER")
    void shouldReturnUserAds_whenGetAdsMeAsUser() throws Exception {
        UserEntity user = createUser("me@test.ru", Role.USER);
        createAd(user);

        mockMvc.perform(get("/ads/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count", is(1)))
                .andExpect(jsonPath("$.results[0].title", notNullValue()));
    }

    @Test
    @DisplayName("POST /ads multipart с ролью USER -> 201 и Ad DTO")
    @WithMockUser(username = "creator@test.ru", roles = "USER")
    void shouldCreateAd_whenUserRole() throws Exception {
        createUser("creator@test.ru", Role.USER);

        CreateOrUpdateAd props = new CreateOrUpdateAd();
        props.setTitle("New title");
        props.setPrice(1500);
        props.setDescription("Very nice description with enough length");

        MockMultipartFile properties = new MockMultipartFile(
                "properties",
                "properties.json",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(props)
        );

        MockMultipartFile image = loadTestJpg(); // ВАЖНО: реальная картинка

        mockMvc.perform(multipart("/ads")
                        .file(properties)
                        .file(image)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.pk", notNullValue()))
                .andExpect(jsonPath("$.title", is("New title")))
                .andExpect(jsonPath("$.price", is(1500)))
                .andExpect(jsonPath("$.author", notNullValue()));
    }

    @Test
    @DisplayName("PATCH /ads/{id} не автор -> 403")
    @WithMockUser(username = "not-owner@test.ru", roles = "USER")
    void shouldReturn403_whenUpdateByNotOwner() throws Exception {
        UserEntity owner = createUser("owner@test.ru", Role.USER);
        createUser("not-owner@test.ru", Role.USER);

        AdEntity ad = createAd(owner);

        CreateOrUpdateAd update = new CreateOrUpdateAd();
        update.setTitle("Updated");
        update.setPrice(200);
        update.setDescription("Updated description");

        mockMvc.perform(patch("/ads/{id}", ad.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PATCH /ads/{id} ADMIN -> 200")
    @WithMockUser(username = "admin@test.ru", roles = "ADMIN")
    void shouldUpdateAd_whenAdminRole() throws Exception {
        createUser("admin@test.ru", Role.ADMIN);

        UserEntity owner = createUser("owner2@test.ru", Role.USER);
        AdEntity ad = createAd(owner);

        CreateOrUpdateAd update = new CreateOrUpdateAd();
        update.setTitle("Admin updated");
        update.setPrice(999);
        update.setDescription("Admin updated description");

        mockMvc.perform(patch("/ads/{id}", ad.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Admin updated")))
                .andExpect(jsonPath("$.price", is(999)));
    }

    @Test
    @DisplayName("DELETE /ads/{id} автор -> 204")
    @WithMockUser(username = "del@test.ru", roles = "USER")
    void shouldDeleteAd_whenOwner() throws Exception {
        UserEntity owner = createUser("del@test.ru", Role.USER);
        AdEntity ad = createAd(owner);

        mockMvc.perform(delete("/ads/{id}", ad.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /ads/{id} без авторизации -> 401")
    void shouldReturn401_whenGetAdByIdWithoutAuth() throws Exception {
        mockMvc.perform(get("/ads/{id}", 1))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /ads/{id} USER -> 200 и структура ExtendedAd")
    @WithMockUser(username = "viewer-ad@test.ru", roles = "USER")
    void shouldReturnExtendedAd_whenGetAdByIdAsUser() throws Exception {
        createUser("viewer-ad@test.ru", Role.USER);

        UserEntity owner = createUser("owner-ext@test.ru", Role.USER);
        AdEntity ad = createAd(owner);

        mockMvc.perform(get("/ads/{id}", ad.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.pk", notNullValue()))
                .andExpect(jsonPath("$.title", notNullValue()))
                .andExpect(jsonPath("$.price", notNullValue()))
                .andExpect(jsonPath("$.description", notNullValue()))
                .andExpect(jsonPath("$.email", notNullValue()))
                .andExpect(jsonPath("$.phone", notNullValue()))
                .andExpect(jsonPath("$.authorFirstName", notNullValue()))
                .andExpect(jsonPath("$.authorLastName", notNullValue()));
    }

    @Test
    @DisplayName("PATCH /ads/{id}/image без авторизации -> 401")
    void shouldReturn401_whenUpdateAdImageWithoutAuth() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "image", "img.png", "image/png", "fake-image".getBytes());

        mockMvc.perform(multipart("/ads/{id}/image", 1)
                        .file(image)
                        .with(request -> { request.setMethod("PATCH"); return request; }))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PATCH /ads/{id}/image не владелец -> 403")
    @WithMockUser(username = "not-owner-img@test.ru", roles = "USER")
    void shouldReturn403_whenUpdateAdImageByNotOwner() throws Exception {
        UserEntity owner = createUser("owner-img@test.ru", Role.USER);
        createUser("not-owner-img@test.ru", Role.USER);

        AdEntity ad = createAd(owner);

        MockMultipartFile image = new MockMultipartFile(
                "image", "img.png", "image/png", "fake-image".getBytes());

        mockMvc.perform(multipart("/ads/{id}/image", ad.getId())
                        .file(image)
                        .with(request -> { request.setMethod("PATCH"); return request; }))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PATCH /ads/{id}/image владелец -> 200")
    @WithMockUser(username = "owner-img2@test.ru", roles = "USER")
    void shouldReturn200_whenUpdateAdImageByOwner() throws Exception {
        UserEntity owner = createUser("owner-img2@test.ru", Role.USER);
        AdEntity ad = createAd(owner);

        MockMultipartFile image = loadTestJpg(); // ВАЖНО: реальная картинка

        mockMvc.perform(multipart("/ads/{id}/image", ad.getId())
                        .file(image)
                        .with(r -> { r.setMethod("PATCH"); return r; }))
                .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

}