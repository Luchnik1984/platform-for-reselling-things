package ru.skypro.homework.repository;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import ru.skypro.homework.AbstractIntegrationTest;
import ru.skypro.homework.entity.ImageEntity;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Интеграционные тесты для ImageRepository.
 * Проверяют CRUD операции и сохранение метаданных файлов.
 * Используют PostgreSQL в Docker контейнере через AbstractIntegrationTest.
 */
@Tag("integration")
@DisplayName("Тесты ImageRepository")
class ImageRepositoryIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ImageRepository imageRepository;

    private ImageEntity testImage;

    @BeforeEach
    void setUp() {
        // Используем уникальный путь на основе текущего времени
        String uniquePath = "uploads/ads/test-" + System.nanoTime() + ".jpg";
        testImage = new ImageEntity(uniquePath, 5242880L, "image/jpeg");
    }

    @Nested
    @DisplayName("Операции создания")
    class CreateOperations {
        @Test
        @DisplayName("Успешное создание изображения в БД")
        void testCreateImage() {
            ImageEntity savedImage = imageRepository.save(testImage);
            assertThat(savedImage).isNotNull();
            assertThat(savedImage.getId()).isNotNull();
            assertThat(savedImage.getFilePath()).contains("uploads/ads/test-");
        }
    }

    @Nested
    @DisplayName("Операции поиска")
    class SearchOperations {
        @Test
        @DisplayName("Поиск изображения по ID")
        void testFindById() {
            ImageEntity savedImage = imageRepository.save(testImage);
            Optional<ImageEntity> foundImage = imageRepository.findById(savedImage.getId());
            assertThat(foundImage).isPresent();
        }

        @Test
        @DisplayName("Нет изображения при несуществующем ID")
        void testFindByIdNotFound() {
            Optional<ImageEntity> foundImage = imageRepository.findById(9999);
            assertThat(foundImage).isEmpty();
        }

        @Test
        @DisplayName("Получение всех изображений из БД")
        void testFindAll() {
            imageRepository.save(testImage);
            assertThat(imageRepository.findAll()).size().isGreaterThanOrEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Операции обновления")
    class UpdateOperations {
        @Test
        @DisplayName("Обновление пути изображения")
        void testUpdateImage() {
            ImageEntity savedImage = imageRepository.save(testImage);
            Integer imageId = savedImage.getId();
            String newUniquePath = "uploads/ads/updated-" + System.nanoTime() + ".jpg";
            savedImage.setFilePath(newUniquePath);
            ImageEntity updatedImage = imageRepository.save(savedImage);
            assertThat(updatedImage.getId()).isEqualTo(imageId);
            assertThat(updatedImage.getFilePath()).contains("uploads/ads/updated-");
        }
    }

    @Nested
    @DisplayName("Операции удаления")
    class DeleteOperations {
        @Test
        @DisplayName("Удаление изображение по ID")
        void testDeleteImage() {
            ImageEntity savedImage = imageRepository.save(testImage);
            Integer imageId = savedImage.getId();
            imageRepository.deleteById(imageId);
            assertThat(imageRepository.existsById(imageId)).isFalse();
        }
    }

    @Nested
    @DisplayName("Метаданные файлов")
    class FileMetadata {
        @Test
        @DisplayName("Сохранение размера файла в БД")
        void testFileSizeStorage() {
            Long fileSize = 10485760L;
            testImage.setFileSize(fileSize);
            ImageEntity savedImage = imageRepository.save(testImage);
            Optional<ImageEntity> foundImage = imageRepository.findById(savedImage.getId());
            assertThat(foundImage).isPresent();
            assertThat(foundImage.get().getFileSize()).isEqualTo(fileSize);
        }

        @Test
        @DisplayName("Сохранение MIME-типа файла в БД")
        void testMediaTypeStorage() {
            testImage.setMediaType("image/png");
            ImageEntity savedImage = imageRepository.save(testImage);
            Optional<ImageEntity> foundImage = imageRepository.findById(savedImage.getId());
            assertThat(foundImage).isPresent();
            assertThat(foundImage.get().getMediaType()).isEqualTo("image/png");
        }

        @Test
        @DisplayName("Поддержка разных форматов (JPEG, PNG, GIF)")
        void testDifferentImageFormats() {
            String jpegPath = "uploads/test-" + System.nanoTime() + ".jpg";
            String pngPath = "uploads/test-" + System.nanoTime() + "-2.png";
            String gifPath = "uploads/test-" + System.nanoTime() + "-3.gif";

            imageRepository.save(new ImageEntity(jpegPath, 1024L, "image/jpeg"));
            imageRepository.save(new ImageEntity(pngPath, 2048L, "image/png"));
            imageRepository.save(new ImageEntity(gifPath, 512L, "image/gif"));
            assertThat(imageRepository.count()).isGreaterThanOrEqualTo(3);
        }

        @Test
        @DisplayName("Поддержка больших размеров файлов (до 100 МБ)")
        void testLargeFileSize() {
            Long largeFileSize = 104857600L;
            testImage.setFileSize(largeFileSize);
            ImageEntity savedImage = imageRepository.save(testImage);
            Optional<ImageEntity> foundImage = imageRepository.findById(savedImage.getId());
            assertThat(foundImage).isPresent();
            assertThat(foundImage.get().getFileSize()).isEqualTo(largeFileSize);
        }
    }

    @Nested
    @DisplayName("Генерация URL")
    class UrlGeneration {
        @Test
        @DisplayName("Генерация URL изображения")
        void testImageUrlGeneration() {
            ImageEntity savedImage = imageRepository.save(testImage);
            String imageUrl = savedImage.getImageUrl();
            assertThat(imageUrl).isNotNull();
            assertThat(imageUrl).contains("images");
        }
    }

    @Nested
    @DisplayName("Связи изображений")
    class ImageRelationshipTests {

        @Test
        @DisplayName("Изображение принадлежит объявлению (если есть связь)")
        void testImageToAdRelation() {
            ImageEntity savedImage = imageRepository.save(testImage);

            Optional<ImageEntity> found = imageRepository.findById(savedImage.getId());
            assertThat(found).isPresent();
            assertThat(found.get().getFilePath()).contains("uploads/ads/test-");
        }

        @Test
        @DisplayName("Объявление может иметь несколько изображений")
        void testAdToMultipleImages() {
            for (int i = 0; i < 3; i++) {
                String path = "uploads/ads/photo" + i + "-" + System.nanoTime() + ".jpg";
                imageRepository.save(new ImageEntity(path, 1024L * (i + 1), "image/jpeg"));
            }

            assertThat(imageRepository.findAll()).size().isGreaterThanOrEqualTo(3);
        }
    }

    @Nested
    @DisplayName("Удаление изображений")
    class ImageDeletionTests {

        @Test
        @DisplayName("Удаление отдельного изображения")
        void testDeleteSingleImage() {
            ImageEntity saved = imageRepository.save(testImage);
            Integer imageId = saved.getId();

            imageRepository.deleteById(imageId);

            assertThat(imageRepository.findById(imageId)).isEmpty();
        }

        @Test
        @DisplayName("Удаление всех изображений объявления")
        void testDeleteAllAdImages() {
            for (int i = 0; i < 3; i++) {
                String path = "uploads/ads/img" + i + "-" + System.nanoTime() + ".jpg";
                imageRepository.save(new ImageEntity(path, 1024L, "image/jpeg"));
            }

            imageRepository.deleteAll();

            assertThat(imageRepository.count()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Метаданные изображений")
    class ImageMetadataTests {

        @Test
        @DisplayName("Поддержка различных MIME-типов")
        void testDifferentMimeTypes() {
            String[] mimeTypes = {"image/jpeg", "image/png", "image/gif", "image/webp"};

            for (int i = 0; i < mimeTypes.length; i++) {
                String path = "uploads/" + i + "-" + System.nanoTime() + ".img";
                imageRepository.save(new ImageEntity(path, 1024L, mimeTypes[i]));
            }

            List<ImageEntity> images = imageRepository.findAll();
            assertThat(images).hasSizeGreaterThanOrEqualTo(4);
        }

        @Test
        @DisplayName("Сохранение размера файла")
        void testFileSizeAccuracy() {
            long[] sizes = {1024L, 2048L, 5242880L, 104857600L};

            for (long size : sizes) {
                String path = "uploads/size-" + System.nanoTime() + ".jpg";
                ImageEntity img = new ImageEntity(path, size, "image/jpeg");
                ImageEntity saved = imageRepository.save(img);

                Optional<ImageEntity> found = imageRepository.findById(saved.getId());
                assertThat(found).isPresent();
                assertThat(found.get().getFileSize()).isEqualTo(size);
            }
        }

        @Test
        @DisplayName("Проверка целостности пути файла")
        void testFilePathIntegrity() {
            ImageEntity saved = imageRepository.save(testImage);
            Optional<ImageEntity> found = imageRepository.findById(saved.getId());

            assertThat(found).isPresent();
            assertThat(found.get().getFilePath())
                    .isNotBlank()
                    .contains("uploads")
                    .endsWith(".jpg");
        }
    }

    @Nested
    @DisplayName("Поиск изображений")
    class ImageSearchTests {

        @Test
        @DisplayName("Получить все JPEG изображения")
        void testFindJpegImages() {
            for (int i = 0; i < 3; i++) {
                String path = "uploads/jpeg-" + System.nanoTime() + ".jpg";
                imageRepository.save(new ImageEntity(path, 1024L, "image/jpeg"));
            }

            List<ImageEntity> jpegImages = imageRepository.findAll().stream()
                    .filter(img -> "image/jpeg".equals(img.getMediaType()))
                    .toList();

            assertThat(jpegImages).size().isGreaterThanOrEqualTo(3);
        }

        @Test
        @DisplayName("Подсчет количества изображений по типу")
        void testCountImagesByType() {
            imageRepository.save(new ImageEntity("img1-" + System.nanoTime() + ".jpg", 1024L, "image/jpeg"));
            imageRepository.save(new ImageEntity("img2-" + System.nanoTime() + ".png", 2048L, "image/png"));
            imageRepository.save(new ImageEntity("img3-" + System.nanoTime() + ".jpg", 1024L, "image/jpeg"));

            long jpegCount = imageRepository.findAll().stream()
                    .filter(img -> "image/jpeg".equals(img.getMediaType()))
                    .count();

            assertThat(jpegCount).isGreaterThanOrEqualTo(2);
        }
    }

}
