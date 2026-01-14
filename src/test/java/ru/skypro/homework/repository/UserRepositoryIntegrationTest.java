package ru.skypro.homework.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import ru.skypro.homework.AbstractIntegrationTest;
import ru.skypro.homework.entity.AdEntity;
import ru.skypro.homework.entity.CommentEntity;
import ru.skypro.homework.entity.UserEntity;
import ru.skypro.homework.enums.Role;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Интеграционные тесты для UserRepository.
 * Проверяют CRUD операции и кастомные методы поиска пользователей.
 * Используют PostgreSQL в Docker контейнере через AbstractIntegrationTest.
 * Каждый тест использует уникальный email.
 */
@SpringBootTest
@DisplayName("Тесты UserRepository")
class UserRepositoryIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    private UserEntity testUser;

    @Autowired
    private AdRepository adRepository;

    @Autowired
    private CommentRepository commentRepository;


    private String generateUniqueEmail(String baseName) {
        long suffix = Math.abs(System.nanoTime()) % 1_000_000; // 6 цифр
        return baseName + suffix + "@m.ru";
    }

    @BeforeEach
    void setUp() {
        testUser = new UserEntity(generateUniqueEmail("testuser"), "password123",
                "Test", "User", "+7 999 123-45-67", Role.USER);

        testUser = userRepository.save(testUser); // ✅ важно
    }


    @Nested
    @DisplayName("Операции создания")
    class CreateOperations {
        @Test
        @DisplayName("Успешное создание пользователя в БД")
        void testCreateUser() {
            UserEntity savedUser = userRepository.save(testUser);
            assertThat(savedUser).isNotNull();
            assertThat(savedUser.getId()).isNotNull();
            assertThat(savedUser.getEmail()).isEqualTo(testUser.getEmail());
        }

        @Test
        @DisplayName("Новый пользователь имеет роль USER по умолчанию")
        void testDefaultRole() {
            UserEntity savedUser = userRepository.save(testUser);
            assertThat(savedUser.getRole()).isEqualTo(Role.USER);
        }

        @Test
        @DisplayName("Новый пользователь включен (enabled) по умолчанию")
        void testDefaultEnabled() {
            UserEntity savedUser = userRepository.save(testUser);
            assertThat(savedUser.isEnabled()).isTrue();
        }
    }

    @Nested
    @DisplayName("Операции поиска")
    class SearchOperations {
        @Test
        @DisplayName("Поиск пользователя по email")
        void testFindByEmail() {
            userRepository.save(testUser);
            Optional<UserEntity> foundUser = userRepository.findByEmail(testUser.getEmail());
            assertThat(foundUser).isPresent();
        }

        @Test
        @DisplayName("Поиск несуществующего пользователя по email")
        void testFindByEmailNotFound() {
            Optional<UserEntity> foundUser = userRepository.findByEmail("notfound@m.ru");
            assertThat(foundUser).isEmpty();
        }

        @Test
        @DisplayName("Проверка существования пользователя по email")
        void testExistsByEmail() {
            userRepository.save(testUser);
            boolean exists = userRepository.existsByEmail(testUser.getEmail());
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("Проверка того, что email не существует")
        void testExistsByEmailNotFound() {
            boolean exists = userRepository.existsByEmail("nonexistent@m.ru");
            assertThat(exists).isFalse();
        }

        @Test
        @DisplayName("Получение всех пользователей из БД")
        void testFindAll() {
            userRepository.save(testUser);
            assertThat(userRepository.findAll()).size().isGreaterThanOrEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Операции обновления")
    class UpdateOperations {
        @Test
        @DisplayName("Обновление данных пользователя (имя)")
        void testUpdateUser() {
            UserEntity savedUser = userRepository.save(testUser);
            Integer userId = savedUser.getId();
            savedUser.setFirstName("Updated");
            UserEntity updatedUser = userRepository.save(savedUser);

            assertThat(updatedUser.getId()).isEqualTo(userId);
            assertThat(updatedUser.getFirstName()).isEqualTo("Updated");
        }
    }

    @Nested
    @DisplayName("Операции удаления")
    class DeleteOperations {
        @Test
        @DisplayName("Удаление пользователя по ID")
        void testDeleteUser() {
            UserEntity savedUser = userRepository.save(testUser);
            Integer userId = savedUser.getId();
            userRepository.deleteById(userId);

            assertThat(userRepository.existsById(userId)).isFalse();
        }
    }

    @Nested
    @DisplayName("🔗 Связи пользователей - НОВОЕ")
    class UserRelationshipTests {

        @Test
        @DisplayName("✅ Пользователь может иметь несколько объявлений")
        void testUserToManyAdsRelation() {
            UserEntity user = userRepository.save(testUser);

            for (int i = 0; i < 3; i++) {
                AdEntity ad = new AdEntity();
                ad.setTitle("Ad " + i);
                ad.setDescription("Valid description");
                ad.setPrice(5000 * (i + 1));
                ad.setAuthor(user);
                adRepository.save(ad);
            }

            List<AdEntity> userAds = adRepository.findAllByAuthorId(user.getId());
            assertThat(userAds).hasSize(3);
        }

        @Test
        @DisplayName("✅ Пользователь может создавать комментарии")
        void testUserToCommentsRelation() {
            UserEntity user = userRepository.save(testUser);
            UserEntity seller = new UserEntity(generateUniqueEmail("seller"), "password123",
                    "Seller", "User", "+7 999 222-22-22", Role.USER);
            seller = userRepository.save(seller);

            AdEntity ad = new AdEntity();
            ad.setTitle("Test Ad");
            ad.setDescription("Valid description");
            ad.setPrice(10000);
            ad.setAuthor(seller);
            ad = adRepository.save(ad);

            for (int i = 0; i < 2; i++) {
                CommentEntity comment = new CommentEntity("Comment " + i, user, ad);
                commentRepository.save(comment);
            }

            List<CommentEntity> userComments = commentRepository.findAll().stream()
                    .filter(c -> c.getAuthor().getId().equals(user.getId()))
                    .toList();

            assertThat(userComments).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Каскадное удаление для пользователя")
    class UserCascadeDeleteTests {

        @Test
        @DisplayName("Удаление всех объявлений пользователя, при его удалении")
        void testCascadeDeleteAllUserAds() {
            UserEntity user = userRepository.save(testUser);

            for (int i = 0; i < 3; i++) {
                AdEntity ad = new AdEntity();
                ad.setTitle("Ad " + i);
                ad.setDescription("Valid description");
                ad.setPrice(10000);
                ad.setAuthor(user);
                adRepository.save(ad);
            }

            Integer userId = user.getId();
            assertThat(adRepository.findAllByAuthorId(userId)).hasSize(3);

            userRepository.deleteById(userId);

            assertThat(adRepository.findAllByAuthorId(userId)).isEmpty();
        }

        @Test
        @DisplayName("-Удаление всех комментариев пользователя, при его удалении")
        void testCascadeDeleteAllUserComments() {
            UserEntity commenter = userRepository.save(testUser);
            UserEntity seller = new UserEntity(generateUniqueEmail("seller"), "password123",
                    "Seller", "User", "+7 999 222-22-22", Role.USER);
            seller = userRepository.save(seller);

            AdEntity ad = new AdEntity();
            ad.setTitle("Test Ad");
            ad.setDescription("Valid description");
            ad.setPrice(10000);
            ad.setAuthor(seller);
            ad = adRepository.save(ad);

            for (int i = 0; i < 3; i++) {
                CommentEntity comment = new CommentEntity("Comment " + i, commenter, ad);
                commentRepository.save(comment);
            }

            Integer commenterId = commenter.getId();
            long initialCount = commentRepository.findAll().stream()
                    .filter(c -> c.getAuthor().getId().equals(commenterId))
                    .count();
            assertThat(initialCount).isEqualTo(3);

            userRepository.deleteById(commenterId);

            long finalCount = commentRepository.findAll().stream()
                    .filter(c -> c.getAuthor().getId().equals(commenterId))
                    .count();
            assertThat(finalCount).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Проверка целостности данных пользователя")
    class UserDataIntegrityTests {

        @Test
        @DisplayName("Email уникален в системе")
        void testEmailUniqueness() {
            UserEntity user1 = new UserEntity(generateUniqueEmail("unique"), "password123",
                    "User", "One", "+7 999 111-11-11", Role.USER);
            user1 = userRepository.save(user1);

            UserEntity user2 = new UserEntity(user1.getEmail(), "password456",
                    "User", "Two", "+7 999 222-22-22", Role.USER);

            assertThatThrownBy(() -> userRepository.save(user2))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Все обязательные поля пользователя заполнены")
        void testUserRequiredFields() {
            UserEntity user = userRepository.save(testUser);
            Optional<UserEntity> found = userRepository.findById(user.getId());

            assertThat(found).isPresent();
            assertThat(found.get().getEmail()).isNotBlank();
            assertThat(found.get().getPassword()).isNotBlank();
            assertThat(found.get().getFirstName()).isNotBlank();
            assertThat(found.get().getLastName()).isNotBlank();
            assertThat(found.get().getPhone()).isNotBlank();
            assertThat(found.get().getRole()).isNotNull();
        }

        @Test
        @DisplayName("Email соответствует формату")
        void testEmailFormat() {
            UserEntity user = userRepository.save(testUser);
            Optional<UserEntity> found = userRepository.findById(user.getId());

            assertThat(found).isPresent();
            assertThat(found.get().getEmail())
                    .contains("@")
                    .contains(".");
        }
    }

    @Nested
    @DisplayName("Статистика пользователей")
    class UserStatisticsTests {

        @Test
        @DisplayName("Подсчет объявлений пользователя")
        void testCountUserAds() {
            UserEntity user = userRepository.save(testUser);

            for (int i = 0; i < 5; i++) {
                AdEntity ad = new AdEntity();
                ad.setTitle("Ad " + i);
                ad.setDescription("Valid description");
                ad.setPrice(10000);
                ad.setAuthor(user);
                adRepository.save(ad);
            }

            int adCount = adRepository.findAllByAuthorId(user.getId()).size();
            assertThat(adCount).isEqualTo(5);
        }

        @Test
        @DisplayName("Подсчет комментариев пользователя")
        void testCountUserComments() {
            UserEntity commenter = userRepository.save(testUser);
            UserEntity seller = new UserEntity(generateUniqueEmail("seller"), "password123",
                    "Seller", "User", "+7 999 222-22-22", Role.USER);
            seller = userRepository.save(seller);

            AdEntity ad = new AdEntity();
            ad.setTitle("Test Ad");
            ad.setDescription("Valid description");
            ad.setPrice(10000);
            ad.setAuthor(seller);
            ad = adRepository.save(ad);

            for (int i = 0; i < 4; i++) {
                CommentEntity comment = new CommentEntity("Comment " + i, commenter, ad);
                commentRepository.save(comment);
            }

            long commentCount = commentRepository.findAll().stream()
                    .filter(c -> c.getAuthor().getId().equals(commenter.getId()))
                    .count();

            assertThat(commentCount).isEqualTo(4);
        }
    }

}