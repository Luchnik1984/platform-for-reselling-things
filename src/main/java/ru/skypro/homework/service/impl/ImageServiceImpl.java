package ru.skypro.homework.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.user.User;
import ru.skypro.homework.repository.AdRepository;
import ru.skypro.homework.repository.UserRepository;
import ru.skypro.homework.service.ImageService;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import static org.flywaydb.core.internal.util.StringUtils.getFileExtension;


/**
 * Сервис для управления изображениями в приложении.
 * Обеспечивает загрузку, хранение и получение изображений
 * для пользовательских аватаров и объявлений.
 * <p>
 * Все изображения сохраняются в файловой системе с организацией
 * по категориям (аватары, объявления).
 * Для предотвращения коллизий используются уникальные имена файлов.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

    private final UserRepository userRepository;
    private final AdRepository adRepository;

    // Конфигурация путей
    private static final String UPLOAD_DIR = "uploads";
    private static final String AVATARS_DIR = UPLOAD_DIR + "/avatars";
    private static final String ADS_DIR = UPLOAD_DIR + "/ads";

    // Допустимые форматы изображений
    private static final String[] ALLOWED_CONTENT_TYPES = {
            "image/jpeg", "image/png", "image/gif", "image/webp"
    };

    // Максимальный размер файла (5MB)
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    //Размеры изображения сохраняемые на локальном носителе
    private static final int ADS_WIDTH = 800;
    private static final int ADS_HEIGHT = 800;
    private static final int AVATAR_WIDTH = 100;
    private static final int AVATAR_HEIGHT = 100;


    /**
     * Сохраняет переданный файл изображения в файловой системе.
     * Генерирует уникальное имя файла для избежания коллизий.
     *
     * @param image           загружаемый файл изображения
     * @param imageUploadPath путь, куда сохранять файл
     * @param width           ширина изображения
     * @param height          высота изображения
     * @return относительный путь к сохранённому файлу (например, ".images/ads_123.jpg")
     */
    @Override
    public String uploadImage(MultipartFile image, String imageUploadPath, int width, int height) {
        try {
            // Создаем директорию, если её нет
            Path uploadPath = Paths.get(imageUploadPath);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Генерируем уникальное имя файла
            String originalFilename = StringUtils.cleanPath(image.getOriginalFilename());
            String fileExtension = getFileExtension(originalFilename);
            String uniqueFilename = UUID.randomUUID() + fileExtension;

            // Сохраняем файл
            Path filePath = uploadPath.resolve(uniqueFilename);
            Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Изменение размера
            resizeImage(filePath.toString(), width, height);

            String imagePath = "/" + imageUploadPath + "/" + uniqueFilename;
            log.info("Изображение сохранено: {}", imagePath);
            return imagePath;

        } catch (IOException e) {
            log.error("Ошибка при сохранении изображения", e);
            throw new RuntimeException("Не удалось сохранить изображение", e);
        }
    }

    /**
     * Получает сохранённое изображение по относительному пути к файлу.
     *
     * @param filePath относительный путь к файлу
     * @return массив байтов содержимого изображения
     */
    @Override
    public byte[] getImage(String filePath) {
        try {
            // Безопасная проверка имени файла
            if (filePath == null || filePath.contains("..") || filePath.contains("/")) {
                throw new IllegalArgumentException("Некорректное имя файла");
            }

            // Ищем файл в разных директориях
            Path[] possiblePaths = {
                    Paths.get(AVATARS_DIR, filePath),
                    Paths.get(ADS_DIR, filePath)
            };

            for (Path path : possiblePaths) {
                if (Files.exists(path) && Files.isReadable(path)) {
                    return Files.readAllBytes(path);
                }
            }

            throw new FileNotFoundException("Файл не найден: " + filePath);

        } catch (IOException e) {
            log.error("Ошибка при чтении изображения: {}", filePath, e);
            throw new RuntimeException("Не удалось прочитать изображение", e);
        }
    }


    /**
     * Сохраняет аватар (изображение профиля) для конкретного пользователя.
     * Автоматически удаляет предыдущий аватар пользователя, если он существует.
     *
     * @param userId идентификатор пользователя
     * @param image  файл изображения аватара
     * @return относительный путь к сохранённому аватару (например, "/avatars/user_123.jpg")
     */
    @Override
    public String uploadUserImage(Integer userId, MultipartFile image) {
        return "";
    }

    /**
     * Сохраняет изображение для конкретного объявления.
     * Автоматически удаляет предыдущие изображения этого объявления при необходимости.
     *
     * @param adId  идентификатор объявления
     * @param image файл изображения объявления
     * @return относительный путь к сохранённому изображению (например, "/ads/ad_456.jpg")
     */
    @Override
    public String uploadAdImage(Integer adId, MultipartFile image) {
        return "";
    }

    /**
     * Изменяет размер изображения до указанных размеров.
     * Если изображение меньше целевых размеров - изменения не производит.
     * Перезаписывает исходный файл.
     *
     * @param filePath     путь к файлу изображения
     * @param targetWidth  максимальная ширина
     * @param targetHeight максимальная высота
     */
    private void resizeImage(String filePath, int targetWidth, int targetHeight) {
        try {
            File inputFile = new File(filePath);
            BufferedImage originalImage = ImageIO.read(inputFile);

            if (originalImage == null) {
                return;
            }

            // Проверяем, нужно ли изменять размер
            if (originalImage.getWidth() <= targetWidth && originalImage.getHeight() <= targetHeight) {
                return;
            }

            // Создаем изображение с новым размером
            BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, originalImage.getType());
            java.awt.Graphics2D g = resizedImage.createGraphics();
            g.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
            g.dispose();

            // Сохраняем с тем же форматом
            String formatName = getFileExtension(inputFile.getName()).substring(1);
            ImageIO.write(resizedImage, formatName, inputFile);

            log.debug("Изображение {} изменено до {}x{}", filePath, targetWidth, targetHeight);

        } catch (IOException e) {
            log.warn("Не удалось изменить размер изображения: {}", filePath, e);
        }
    }
}
