package ru.skypro.homework.controller.integrationTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import ru.skypro.homework.AbstractIntegrationTest;
import ru.skypro.homework.dto.comments.CreateOrUpdateComment;
import ru.skypro.homework.entity.AdEntity;
import ru.skypro.homework.entity.CommentEntity;
import ru.skypro.homework.entity.UserEntity;
import ru.skypro.homework.enums.Role;
import ru.skypro.homework.repository.AdRepository;
import ru.skypro.homework.repository.CommentRepository;
import ru.skypro.homework.repository.UserRepository;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Интеграционные тесты {@code CommentController}.
 * <p>
 * Покрывает эндпоинты:
 * <ul>
 *   <li>GET /ads/{id}/comments</li>
 *   <li>POST /ads/{id}/comments</li>
 *   <li>PATCH /ads/{adId}/comments/{commentId}</li>
 *   <li>DELETE /ads/{adId}/comments/{commentId}</li>
 * </ul>
 */
@AutoConfigureMockMvc
@DisplayName("CommentController Integration Tests")
class CommentControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Autowired private CommentRepository commentRepository;
    @Autowired private AdRepository adRepository;
    @Autowired private UserRepository userRepository;

    @Autowired private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        commentRepository.deleteAll();
        adRepository.deleteAll();
        userRepository.deleteAll();
    }

    private UserEntity createUser(String email, Role role) {
        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setRole(role);
        user.setEnabled(true);
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPhone("+7 999 111-11-11");
        user.setPassword(passwordEncoder.encode("password123"));
        return userRepository.save(user);
    }

    private AdEntity createAd(UserEntity author) {
        AdEntity ad = new AdEntity();
        ad.setTitle("Ad title");
        ad.setDescription("This is a valid description with at least 8 chars");
        ad.setPrice(100);
        ad.setAuthor(author);
        return adRepository.save(ad);
    }

    private CommentEntity createComment(UserEntity author, AdEntity ad, String text) {
        CommentEntity c = new CommentEntity();
        c.setAuthor(author);
        c.setAd(ad);
        c.setText(text);
        c.setCreatedAt(LocalDateTime.now());
        return commentRepository.save(c);
    }

    @Test
    @DisplayName("GET /ads/{id}/comments без auth -> 401")
    void shouldReturn401_whenGetCommentsWithoutAuth() throws Exception {
        mockMvc.perform(get("/ads/{id}/comments", 1))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /ads/{id}/comments USER -> 200 и Comment DTO")
    @WithMockUser(username = "commenter@test.ru", roles = "USER")
    void shouldAddComment_whenUserRole() throws Exception {
        createUser("commenter@test.ru", Role.USER); // достаточно создать, переменная не нужна
        UserEntity adOwner = createUser("ad-owner@test.ru", Role.USER);
        AdEntity ad = createAd(adOwner);

        CreateOrUpdateComment dto = new CreateOrUpdateComment();
        dto.setText("Very good!");

        mockMvc.perform(post("/ads/{id}/comments", ad.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.pk", notNullValue()))
                .andExpect(jsonPath("$.text", is("Very good!")))
                .andExpect(jsonPath("$.author", notNullValue()));
    }


    @Test
    @DisplayName("DELETE /ads/{adId}/comments/{commentId} не автор -> 403")
    @WithMockUser(username = "not-author@test.ru", roles = "USER")
    void shouldReturn403_whenDeleteByNotAuthor() throws Exception {
        UserEntity author = createUser("author@test.ru", Role.USER);
        createUser("not-author@test.ru", Role.USER);

        UserEntity adOwner = createUser("ad-owner2@test.ru", Role.USER);
        AdEntity ad = createAd(adOwner);

        CommentEntity comment = createComment(author, ad, "Comment text long");

        mockMvc.perform(delete("/ads/{adId}/comments/{commentId}", ad.getId(), comment.getId()))
                .andExpect(status().isForbidden());
    }


    @Test
    @DisplayName("PATCH /ads/{adId}/comments/{commentId} ADMIN -> 200")
    @WithMockUser(username = "admin@test.ru", roles = "ADMIN")
    void shouldUpdateComment_whenAdminRole() throws Exception {
        createUser("admin@test.ru", Role.ADMIN);

        UserEntity author = createUser("author2@test.ru", Role.USER);
        UserEntity adOwner = createUser("ad-owner3@test.ru", Role.USER);
        AdEntity ad = createAd(adOwner);

        CommentEntity comment = createComment(author, ad, "Old text long");

        CreateOrUpdateComment dto = new CreateOrUpdateComment();
        dto.setText("New text!!");

        mockMvc.perform(patch("/ads/{adId}/comments/{commentId}", ad.getId(), comment.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pk", notNullValue()))
                .andExpect(jsonPath("$.author", notNullValue()))
                .andExpect(jsonPath("$.createdAt", notNullValue()))
                .andExpect(jsonPath("$.text", is("New text!!")));

    }

    @Test
    @DisplayName("GET /ads/{id}/comments с auth -> 200 и структура Comments")
    @WithMockUser(username = "viewer@test.ru", roles = "USER")
    void shouldReturnComments_whenGetCommentsWithAuth() throws Exception {
        // кто угодно авторизованный (openapi требует авторизацию)
        createUser("viewer@test.ru", Role.USER);

        UserEntity adOwner = createUser("ad-owner-get@test.ru", Role.USER);
        AdEntity ad = createAd(adOwner);

        UserEntity commentAuthor = createUser("comment-author-get@test.ru", Role.USER);
        createComment(commentAuthor, ad, "Comment text long");

        mockMvc.perform(get("/ads/{id}/comments", ad.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count", is(1)))
                .andExpect(jsonPath("$.results", isA(java.util.List.class)))
                .andExpect(jsonPath("$.results[0].pk", notNullValue()))
                .andExpect(jsonPath("$.results[0].text", notNullValue()))
                .andExpect(jsonPath("$.results[0].author", notNullValue()));
    }
    @Test
    @DisplayName("DELETE /ads/{adId}/comments/{commentId} автор -> 200 и комментарий удалён")
    @WithMockUser(username = "author-del@test.ru", roles = "USER")
    void shouldDeleteComment_whenAuthor() throws Exception {
        UserEntity author = createUser("author-del@test.ru", Role.USER);

        UserEntity adOwner = createUser("ad-owner-del@test.ru", Role.USER);
        AdEntity ad = createAd(adOwner);

        CommentEntity comment = createComment(author, ad, "Comment text long");

        mockMvc.perform(delete("/ads/{adId}/comments/{commentId}", ad.getId(), comment.getId()))
                .andExpect(status().isOk());

        org.junit.jupiter.api.Assertions.assertFalse(commentRepository.findById(comment.getId()).isPresent());
    }

    @Test
    @DisplayName("DELETE /ads/{adId}/comments/{commentId} ADMIN -> 200")
    @WithMockUser(username = "admin-del@test.ru", roles = "ADMIN")
    void shouldDeleteComment_whenAdmin() throws Exception {
        createUser("admin-del@test.ru", Role.ADMIN);

        UserEntity author = createUser("author-admin-del@test.ru", Role.USER);
        UserEntity adOwner = createUser("ad-owner-admin-del@test.ru", Role.USER);
        AdEntity ad = createAd(adOwner);

        CommentEntity comment = createComment(author, ad, "Comment text long");

        mockMvc.perform(delete("/ads/{adId}/comments/{commentId}", ad.getId(), comment.getId()))
                .andExpect(status().isOk());
    }

}