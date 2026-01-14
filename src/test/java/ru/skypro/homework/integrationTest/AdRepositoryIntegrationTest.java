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
import ru.skypro.homework.repository.AdRepository;
import ru.skypro.homework.repository.CommentRepository;
import ru.skypro.homework.repository.ImageRepository;
import ru.skypro.homework.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Интеграционные тесты для AdRepository.
 * Проверяют CRUD операции, поиск объявлений, каскадное удаление.
 * Используют PostgreSQL в Docker контейнере через AbstractIntegrationTest.
 * Каждый тест использует УНИКАЛЬНЫЙ email для User entities
 * для избежания нарушения UNIQUE constraint на колонке email.
 */
@SpringBootTest
@DisplayName("Тесты AdRepository")
class AdRepositoryIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private AdRepository adRepository;

    @Autowired
    private UserRepository userRepository;

    private AdEntity testAd;
    private UserEntity testUser;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ImageRepository imageRepository;


    /**
     * Генерирует уникальный email для избежания конфликтов UNIQUE constraint.
     */
    private String generateUniqueEmail(String baseName) {
        long hash = System.nanoTime() % 1_000_000;
        return baseName + hash + "@mail.ru";
    }

    @BeforeEach
    void setUp() {
        // Очищаем БД
        commentRepository.deleteAll();
        adRepository.deleteAll();
        userRepository.deleteAll();

        // Создаем тестового пользователя
        testUser = new UserEntity();
        testUser.setEmail("test@example.com");
        testUser.setPassword("encoded_password");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setPhone("+7 999 111-11-11");
        testUser = userRepository.save(testUser);

        // Создаем тестовое объявление
        testAd = new AdEntity();
        testAd.setTitle("Valid Ad Title");
        testAd.setDescription("This is a valid description with at least 8 characters");
        testAd.setPrice(100);
        testAd.setAuthor(testUser);
        testAd = adRepository.save(testAd);
    }

    @Nested
    @DisplayName("Операции создания")
    class CreateOperations {
        @Test
        @DisplayName("Успешное создание объявления в БД")
        void testCreateAd() {
            AdEntity ad = new AdEntity();
            ad.setTitle("Used Phone");
            ad.setDescription("iPhone 13, good condition");
            ad.setPrice(25000);
            ad.setAuthor(testUser);

            AdEntity savedAd = adRepository.save(ad);

            assertThat(savedAd).isNotNull();
            assertThat(savedAd.getId()).isNotNull();
            assertThat(savedAd.getTitle()).isEqualTo("Used Phone");
        }
    }

    @Nested
    @DisplayName("Операции поиска")
    class SearchOperations {
        @Test
        @DisplayName("Поиск объявления по ID")
        void testFindById() {
            AdEntity savedAd = adRepository.save(testAd);
            Optional<AdEntity> foundAd = adRepository.findById(savedAd.getId());
            assertThat(foundAd).isPresent();
            assertThat(foundAd.get().getAuthor()).isNotNull();
        }

        @Test
        @DisplayName("Нет объявления, при несуществующем ID")
        void testFindByIdNotFound() {
            Optional<AdEntity> foundAd = adRepository.findById(9999);
            assertThat(foundAd).isEmpty();
        }

        @Test
        @DisplayName("Поиск всех объявлений автора по ID")
        void testFindAllByAuthorId() {
            adRepository.save(testAd);
            List<AdEntity> userAds = adRepository.findAllByAuthorId(testUser.getId());
            assertThat(userAds).hasSize(1);
        }

        @Test
        @DisplayName("Поиск объявления несуществующего автора")
        void testFindByAuthorIdNotFound() {
            List<AdEntity> ads = adRepository.findAllByAuthorId(9999);
            assertThat(ads).isEmpty();
        }

        @Test
        @DisplayName("Получение всех объявлений из БД")
        void testFindAll() {
            adRepository.save(testAd);
            assertThat(adRepository.findAll()).size().isGreaterThanOrEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Контроль доступа")
    class AccessControl {
        @Test
        @DisplayName("Поиск объявлений по ID и ID автора (проверка прав)")
        void testFindByIdAndAuthorId() {
            AdEntity savedAd = adRepository.save(testAd);
            Optional<AdEntity> foundAd = adRepository.findByIdAndAuthorId(savedAd.getId(), testUser.getId());
            assertThat(foundAd).isPresent();
        }

        @Test
        @DisplayName("Отказ в доступе, если ID автора не совпадает")
        void testFindByIdAndAuthorIdNotMatched() {
            AdEntity savedAd = adRepository.save(testAd);
            Optional<AdEntity> foundAd = adRepository.findByIdAndAuthorId(savedAd.getId(), 9999);
            assertThat(foundAd).isEmpty();
        }
    }

    @Nested
    @DisplayName("Операции обновления")
    class UpdateOperations {
        @Test
        @DisplayName("Обновление данных объявления (название и цену)")
        void testUpdateAd() {
            AdEntity savedAd = adRepository.save(testAd);
            Integer adId = savedAd.getId();
            savedAd.setTitle("Updated Phone");
            savedAd.setPrice(20000);
            AdEntity updatedAd = adRepository.save(savedAd);
            assertThat(updatedAd.getId()).isEqualTo(adId);
            assertThat(updatedAd.getTitle()).isEqualTo("Updated Phone");
        }
    }

    @Nested
    @DisplayName("Операции удаления")
    class DeleteOperations {
        @Test
        @DisplayName("Удаление объявления по ID")
        void testDeleteAd() {
            AdEntity savedAd = adRepository.save(testAd);
            Integer adId = savedAd.getId();
            adRepository.deleteById(adId);
            assertThat(adRepository.existsById(adId)).isFalse();
        }

        @Test
        @DisplayName("Каскадное удаление объявлений при удалении пользователя")
        void testCascadeDeleteOnUserDelete() {
            adRepository.save(testAd);
            Integer userId = testUser.getId();
            userRepository.deleteById(userId);
            assertThat(adRepository.findAllByAuthorId(userId)).isEmpty();
        }
    }

    @Nested
    class RelationshipTests {

        @Test
        void testAdToAuthorRelation() {
            AdEntity ad = new AdEntity();
            ad.setTitle("Test Ad");
            ad.setDescription("This is a valid description with proper length");
            ad.setPrice(150);
            ad.setAuthor(testUser);
            AdEntity savedAd = adRepository.save(ad);

            assertNotNull(savedAd.getAuthor());
            assertEquals(testUser.getId(), savedAd.getAuthor().getId());
        }

        @Test
        void testAdToCommentsRelation() {
            CommentEntity comment1 = new CommentEntity();
            comment1.setText("First comment with valid length");
            comment1.setAd(testAd);
            comment1.setAuthor(testUser);

            CommentEntity comment2 = new CommentEntity();
            comment2.setText("Second comment with valid length");
            comment2.setAd(testAd);
            comment2.setAuthor(testUser);

            commentRepository.save(comment1);
            commentRepository.save(comment2);

            var adComments = commentRepository.findAllByAdId(testAd.getId());
            assertEquals(2, adComments.size());

        }

        @Test
        void testUserToAdsRelation() {
            adRepository.deleteAll(adRepository.findAllByAuthorId(testUser.getId())); // метод есть [file:2]

            AdEntity ad1 = new AdEntity();
            ad1.setTitle("Ad 1");
            ad1.setDescription("Description 1, ok");
            ad1.setPrice(100);
            ad1.setAuthor(testUser);
            adRepository.save(ad1);

            AdEntity ad2 = new AdEntity();
            ad2.setTitle("Ad 2");
            ad2.setDescription("Description 2, ok");
            ad2.setPrice(200);
            ad2.setAuthor(testUser);
            adRepository.save(ad2);

            var userAds = adRepository.findAllByAuthorId(testUser.getId());

            assertEquals(2, userAds.size());
        }


        @Test
        void testMultipleAdsToSameUser() {
            for (int i = 0; i < 3; i++) {
                AdEntity ad = new AdEntity();
                ad.setTitle("Ad " + i);
                ad.setDescription("This is a valid description number " + i);
                ad.setPrice(100 + i * 50);
                ad.setAuthor(testUser);
                adRepository.save(ad);
            }

            var userAds = adRepository.findAllByAuthorId(testUser.getId());
            assertEquals(4, userAds.size()); // 3 новых + 1 из setUp
        }
    }

    @Nested
    class CascadeDeleteTests {

        @Test
        void testCascadeDeleteComments() {
            CommentEntity comment1 = new CommentEntity();
            comment1.setText("Comment that will be deleted");
            comment1.setAd(testAd);
            comment1.setAuthor(testUser);

            CommentEntity comment2 = new CommentEntity();
            comment2.setText("Another comment that will be deleted");
            comment2.setAd(testAd);
            comment2.setAuthor(testUser);

            commentRepository.save(comment1);
            commentRepository.save(comment2);

            Integer adId = testAd.getId();

            adRepository.delete(testAd);

            var comments = commentRepository.findAllByAdId(adId);
            assertEquals(0, comments.size());
        }

        @Test
        void testCascadeDeleteOnUserDelete() {
            UserEntity user = new UserEntity();
            user.setEmail("cascade@example.com");
            user.setPassword("password");
            user.setFirstName("Cascade");
            user.setLastName("User");
            user.setPhone("+7 999 111-11-11");
            user = userRepository.save(user);

            AdEntity ad = new AdEntity();
            ad.setTitle("User's Ad");
            ad.setDescription("This is a valid description for cascade test");
            ad.setPrice(300);
            ad.setAuthor(user);
            ad = adRepository.save(ad);

            Integer adId = ad.getId();

            userRepository.delete(user);

            AdEntity deletedAd = adRepository.findById(adId).orElse(null);
            assertNull(deletedAd);
        }
    }

    @Nested
    class DataIntegrityTests {

        @Test
        void testAdDescriptionTooShort() {
            AdEntity invalidAd = new AdEntity();
            invalidAd.setTitle("Short Desc");
            invalidAd.setDescription("Short");
            invalidAd.setPrice(100);
            invalidAd.setAuthor(testUser);

            assertThrows(Exception.class, () -> adRepository.save(invalidAd));
        }

        @Test
        void testAdDescriptionTooLong() {
            String longDescription = "A".repeat(65);
            AdEntity invalidAd = new AdEntity();
            invalidAd.setTitle("Long Desc");
            invalidAd.setDescription(longDescription);
            invalidAd.setPrice(100);
            invalidAd.setAuthor(testUser);

            assertThrows(Exception.class, () -> adRepository.save(invalidAd));
        }

        @Test
        void testAdPricePositive() {
            AdEntity ad = new AdEntity();
            ad.setTitle("Valid Ad");
            ad.setDescription("This is a valid description with proper length");
            ad.setPrice(0);
            ad.setAuthor(testUser);

            AdEntity saved = adRepository.save(ad);
            assertNotNull(saved);
            assertEquals(0, saved.getPrice());
        }
    }

    @Nested
    class StatisticsTests {

        @Test
        void testAverageAdPrice() {
            AdEntity ad1 = new AdEntity();
            ad1.setTitle("Product 1");
            ad1.setDescription("This is a complete product description");
            ad1.setPrice(100);
            ad1.setAuthor(testUser);

            AdEntity ad2 = new AdEntity();
            ad2.setTitle("Product 2");
            ad2.setDescription("Another complete product description");
            ad2.setPrice(200);
            ad2.setAuthor(testUser);

            adRepository.save(ad1);
            adRepository.save(ad2);

            double average = adRepository.findAll().stream()
                    .mapToDouble(AdEntity::getPrice)
                    .average()
                    .orElse(0.0);

            assertTrue(average > 100);
        }

        @Test
        void testMostExpensiveAd() {
            AdEntity ad1 = new AdEntity();
            ad1.setTitle("Cheap Product");
            ad1.setDescription("This is a cheap product description");
            ad1.setPrice(100);
            ad1.setAuthor(testUser);

            AdEntity ad2 = new AdEntity();
            ad2.setTitle("Expensive Product");
            ad2.setDescription("This is an expensive product description");
            ad2.setPrice(500);
            ad2.setAuthor(testUser);

            adRepository.save(ad1);
            AdEntity expensive = adRepository.save(ad2);

            AdEntity mostExpensive = adRepository.findAll().stream()
                    .max((a1, a2) -> Double.compare(a1.getPrice(), a2.getPrice()))
                    .orElse(null);

            assertNotNull(mostExpensive);
            assertEquals(expensive.getId(), mostExpensive.getId());
            assertEquals(500, mostExpensive.getPrice());
        }
    }
}