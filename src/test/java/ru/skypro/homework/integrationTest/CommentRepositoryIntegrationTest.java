package ru.skypro.homework.integrationTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.skypro.homework.AbstractIntegrationTest;
import ru.skypro.homework.entity.AdEntity;
import ru.skypro.homework.entity.CommentEntity;
import ru.skypro.homework.entity.UserEntity;
import ru.skypro.homework.enums.Role;
import ru.skypro.homework.repository.AdRepository;
import ru.skypro.homework.repository.CommentRepository;
import ru.skypro.homework.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Интеграционные тесты для CommentRepository.
 * Проверяют CRUD операции, поиск комментариев, каскадное удаление и временные метки.
 * Используют PostgreSQL в Docker контейнере через AbstractIntegrationTest.
 */
@SpringBootTest
@DisplayName("Тесты CommentRepository")
class CommentRepositoryIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private AdRepository adRepository;

    @Autowired
    private UserRepository userRepository;

    private CommentEntity testComment;

    private AdEntity testAd;
    private UserEntity testUser;

    private UserEntity adAuthor;
    private UserEntity commentAuthor;

    /**
     * Генерирует уникальный email для избежания конфликтов UNIQUE constraint.
     */
    private String generateUniqueEmail(String baseName) {
        long hash = System.nanoTime() % 1_000_000;
        return baseName + hash + "@mail.ru";
    }


    @BeforeEach
    void setUp() {
        adAuthor = new UserEntity(generateUniqueEmail("adauthor"), "password123",
                "Ad", "Author", "+7 999 111-11-11", Role.USER);
        adAuthor = userRepository.save(adAuthor);

        commentAuthor = new UserEntity(generateUniqueEmail("commenter"), "password123",
                "Comment", "Author", "+7 999 222-22-22", Role.USER);
        commentAuthor = userRepository.save(commentAuthor);

        // пользователь для доп.тестов (author в комментариях)
        testUser = new UserEntity(generateUniqueEmail("testuser"), "password123",
                "Test", "User", "+7 999 333-33-33", Role.USER);
        testUser = userRepository.save(testUser);

        testAd = new AdEntity();
        testAd.setTitle("Used Phone");
        testAd.setDescription("iPhone 13");
        testAd.setPrice(25000);
        testAd.setAuthor(adAuthor);
        testAd = adRepository.save(testAd);

        testComment = new CommentEntity("Great phone!", commentAuthor, testAd);
    }


    @Nested
    @DisplayName("Операции создания")
    class CreateOperations {
        @Test
        @DisplayName("Успешное создание комментария в БД")
        void testCreateComment() {
            CommentEntity savedComment = commentRepository.save(testComment);
            assertThat(savedComment).isNotNull();
            assertThat(savedComment.getId()).isNotNull();
            assertThat(savedComment.getText()).isEqualTo("Great phone!");
        }
    }

    @Nested
    @DisplayName("Операции поиска")
    class SearchOperations {
        @Test
        @DisplayName("Поиск всех комментариев объявления по ID")
        void testFindAllByAdId() {
            commentRepository.save(testComment);
            List<CommentEntity> comments = commentRepository.findAllByAdId(testAd.getId());
            assertThat(comments).hasSize(1);
        }

        @Test
        @DisplayName("Поиск комментариев, при несуществующем ID объявления")
        void testFindByAdIdNotFound() {
            List<CommentEntity> comments = commentRepository.findAllByAdId(9999);
            assertThat(comments).isEmpty();
        }


        @Test
        @DisplayName("Поиск комментария со всеми связанными данными (автор)")
        void testFindCommentWithAuthor() {
            CommentEntity savedComment = commentRepository.save(testComment);
            Optional<CommentEntity> foundComment = commentRepository.findById(savedComment.getId());
            assertThat(foundComment).isPresent();
            assertThat(foundComment.get().getAuthor()).isNotNull();
        }

        @Test
        @DisplayName("Нет комментария, при несуществующем ID")
        void testFindByIdNotFound() {
            Optional<CommentEntity> foundComment = commentRepository.findById(9999);
            assertThat(foundComment).isEmpty();
        }
    }

    @Nested
    @DisplayName("Контроль доступа")
    class AccessControl {
        @Test
        @DisplayName("Поиск комментария по ID и ID автора (проверка прав)")
        void testFindByIdAndAuthorId() {
            CommentEntity savedComment = commentRepository.save(testComment);
            Optional<CommentEntity> foundComment = commentRepository.findByIdAndAuthorId(
                    savedComment.getId(), commentAuthor.getId());
            assertThat(foundComment).isPresent();
        }

        @Test
        @DisplayName("Отказ в доступе, если ID автора не совпадает")
        void testFindByIdAndAuthorIdNotMatched() {
            CommentEntity savedComment = commentRepository.save(testComment);
            Optional<CommentEntity> foundComment = commentRepository.findByIdAndAuthorId(
                    savedComment.getId(), 9999);
            assertThat(foundComment).isEmpty();
        }
    }

    @Nested
    @DisplayName("Операции обновления")
    class UpdateOperations {
        @Test
        @DisplayName("Обновление текста комментария")
        void testUpdateComment() {
            CommentEntity savedComment = commentRepository.save(testComment);
            Integer commentId = savedComment.getId();
            savedComment.setText("Updated comment text");
            CommentEntity updatedComment = commentRepository.save(savedComment);
            assertThat(updatedComment.getId()).isEqualTo(commentId);
            assertThat(updatedComment.getText()).isEqualTo("Updated comment text");
        }
    }

    @Nested
    @DisplayName("Операции удаления")
    class DeleteOperations {
        @Test
        @DisplayName("Удаление комментария по ID")
        void testDeleteComment() {
            CommentEntity savedComment = commentRepository.save(testComment);
            Integer commentId = savedComment.getId();
            commentRepository.deleteById(commentId);
            assertThat(commentRepository.existsById(commentId)).isFalse();
        }

        @Test
        @DisplayName("Каскадное удаление комментариев при удалении объявления")
        void testCascadeDeleteOnAdDelete() {
            commentRepository.save(testComment);
            Integer adId = testAd.getId();
            adRepository.deleteById(adId);
            assertThat(commentRepository.findAllByAdId(adId)).isEmpty();
        }
    }

    @Nested
    @DisplayName("Временные метки")
    class TimestampOperations {
        @Test
        @DisplayName("Автоматически устанавливается временная метка (createdAt)")
        void testCreatedAtTimestamp() {
            // arrange: локально создаём автора, объявление и комментарий
            UserEntity author = userRepository.save(
                    new UserEntity(generateUniqueEmail("commenter"), "password123",
                            "Comment", "Author", "+7 999 222-22-22", Role.USER)
            );

            AdEntity ad = new AdEntity();
            ad.setTitle("Used Phone");
            ad.setDescription("iPhone 13, ok");
            ad.setPrice(25000);
            ad.setAuthor(author);
            ad = adRepository.save(ad);

            CommentEntity comment = new CommentEntity("Great phone!", author, ad);

            CommentEntity savedComment = commentRepository.save(comment);

            assertNotNull(savedComment.getCreatedAt());
        }
    }

    @Nested
    class RelationshipTests {

        @Test
        void testCommentToAdRelation() {
            CommentEntity comment = new CommentEntity();
            comment.setText("This is a valid comment text with more than 8 characters");
            comment.setAd(testAd);
            comment.setAuthor(testUser);
            CommentEntity savedComment = commentRepository.save(comment);

            assertNotNull(savedComment.getId());
            assertNotNull(savedComment.getAd());
            assertEquals(testAd.getId(), savedComment.getAd().getId());
        }

        @Test
        void testCommentToAuthorRelation() {
            CommentEntity comment = new CommentEntity();
            comment.setText("This is a valid comment with enough characters");
            comment.setAd(testAd);
            comment.setAuthor(testUser);
            CommentEntity savedComment = commentRepository.save(comment);

            assertNotNull(savedComment.getAuthor());
            assertEquals(testUser.getId(), savedComment.getAuthor().getId());
        }

        @Test
        void testAdToCommentsRelation() {
            CommentEntity comment1 = new CommentEntity();
            comment1.setText("First valid comment with 8+ characters");
            comment1.setAd(testAd);
            comment1.setAuthor(testUser);

            CommentEntity comment2 = new CommentEntity();
            comment2.setText("Second valid comment with 8+ characters");
            comment2.setAd(testAd);
            comment2.setAuthor(testUser);

            commentRepository.save(comment1);
            commentRepository.save(comment2);

            List<CommentEntity> comments = commentRepository.findAllByAdId(testAd.getId());
            assertEquals(2, comments.size());
        }


        @Test
        void testUserToCommentsRelation() {
            CommentEntity comment1 = new CommentEntity();
            comment1.setText("User comment 1 with valid length");
            comment1.setAd(testAd);
            comment1.setAuthor(testUser);

            CommentEntity comment2 = new CommentEntity();
            comment2.setText("User comment 2 with valid length");
            comment2.setAd(testAd);
            comment2.setAuthor(testUser);

            commentRepository.save(comment1);
            commentRepository.save(comment2);

            long commentCount = commentRepository.findAllByAdId(testAd.getId()).stream()
                    .filter(c -> c.getAuthor() != null && c.getAuthor().getId().equals(testUser.getId()))
                    .count();

            assertEquals(2, commentCount);
        }
    }


    @Nested
    class OrphanRemovalTests {

        @Test
        void testOrphanRemovalOnCollectionClear() {
            CommentEntity comment = new CommentEntity();
            comment.setText("This comment will become orphan");
            comment.setAd(testAd);
            comment.setAuthor(testUser);
            CommentEntity savedComment = commentRepository.save(comment);

            Integer commentId = savedComment.getId();

            adRepository.delete(testAd);

            CommentEntity deletedComment = commentRepository.findById(commentId).orElse(null);
            assertNull(deletedComment);
        }
    }

    @Nested
    class DataIntegrityTests {

        @Test
        void testCommentTextTooShort() {
            CommentEntity invalidComment = new CommentEntity();
            invalidComment.setText("Short");
            invalidComment.setAd(testAd);
            invalidComment.setAuthor(testUser);

            assertThrows(Exception.class, () -> commentRepository.save(invalidComment));
        }

        @Test
        void testCommentTextTooLong() {
            String longText = "A".repeat(65);
            CommentEntity invalidComment = new CommentEntity();
            invalidComment.setText(longText);
            invalidComment.setAd(testAd);
            invalidComment.setAuthor(testUser);

            assertThrows(Exception.class, () -> commentRepository.save(invalidComment));
        }
    }

    @Nested
    class AdditionalSearchTests {

        @Test
        void testFindLatestComment() {
            CommentEntity comment1 = new CommentEntity();
            comment1.setText("First comment with valid length");
            comment1.setAd(testAd);
            comment1.setAuthor(testUser);
            commentRepository.save(comment1);

            CommentEntity comment2 = new CommentEntity();
            comment2.setText("Latest comment with valid length");
            comment2.setAd(testAd);
            comment2.setAuthor(testUser);
            commentRepository.save(comment2);

            var comments = commentRepository.findAllByAdId(testAd.getId());
            assertFalse(comments.isEmpty());
            assertEquals(2, comments.size());
        }

        @Test
        void testFindCommentsByAuthor() {
            CommentEntity c1 = new CommentEntity();
            c1.setText("Author comment 1 with proper length");
            c1.setAd(testAd);
            c1.setAuthor(commentAuthor);
            c1 = commentRepository.save(c1);

            CommentEntity c2 = new CommentEntity();
            c2.setText("Author comment 2 with proper length");
            c2.setAd(testAd);
            c2.setAuthor(commentAuthor);
            c2 = commentRepository.save(c2);

            assertTrue(commentRepository.findByIdAndAuthorId(c1.getId(), commentAuthor.getId()).isPresent());
            assertTrue(commentRepository.findByIdAndAuthorId(c2.getId(), commentAuthor.getId()).isPresent());
        }


    }
}