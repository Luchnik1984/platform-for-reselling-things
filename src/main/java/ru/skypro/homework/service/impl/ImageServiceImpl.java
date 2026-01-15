package ru.skypro.homework.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.entity.AdEntity;
import ru.skypro.homework.entity.ImageEntity;
import ru.skypro.homework.entity.UserEntity;
import ru.skypro.homework.exceptions.UserNotFoundException;
import ru.skypro.homework.repository.AdRepository;
import ru.skypro.homework.repository.ImageRepository;
import ru.skypro.homework.repository.UserRepository;
import ru.skypro.homework.service.ImageService;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
@Transactional
public class ImageServiceImpl implements ImageService {

    private final ImageRepository imageRepository;
    private final UserRepository userRepository;
    private final AdRepository adRepository;

    // Конфигурация путей
    private static final String UPLOAD_DIR = "uploads";
    private static final String AVATARS_DIR = Paths.get(UPLOAD_DIR, "avatars").toString();
    private static final String ADS_DIR = Paths.get(UPLOAD_DIR, "ads").toString();


    // Допустимые форматы изображений
    private static final String[] ALLOWED_CONTENT_TYPES = {"image/jpeg", "image/png", "image/gif", "image/webp"};

    // Максимальный размер файла (5MB)
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    //Размеры изображения сохраняемые на локальном носителе
    private static final int ADS_WIDTH = 800;
    private static final int ADS_HEIGHT = 800;
    private static final int AVATAR_WIDTH = 200;
    private static final int AVATAR_HEIGHT = 200;


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
    @Transactional
    public ImageEntity uploadImage(MultipartFile image, String imageUploadPath, int width, int height) {
        // Проверяем, что файл не пустой
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("Файл изображения не может быть пустым");
        }

        // Создаем директорию, если она не существует
        try {
            Path uploadDir = Paths.get(imageUploadPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать директорию для загрузки: " + imageUploadPath, e);
        }

        // Генерируем уникальное имя файла
        String originalFilename = StringUtils.cleanPath(image.getOriginalFilename());
        String fileExtension = getFileExtension(originalFilename);
        String uniqueFileName = UUID.randomUUID() + "." + fileExtension;

        // Формируем полный путь для сохранения файла
        Path filePath = Paths.get(imageUploadPath, uniqueFileName);

        try {
            // Обрабатываем изображение: изменяем размер
            BufferedImage bufferedImage = ImageIO.read(image.getInputStream());

            // Проверяем, что файл является изображением
            if (bufferedImage == null) {
                throw new IllegalArgumentException("Загруженный файл не является изображением");
            }

            // Изменяем размер изображения
            BufferedImage resizedImage = resizeImage(bufferedImage, width, height);

            // Сохраняем изображение в файловую систему
            String formatName = getFormatName(fileExtension);
            ImageIO.write(resizedImage, formatName, filePath.toFile());

            // Получаем размер файла
            long fileSize = Files.size(filePath);

            // Получаем MIME-тип
            String mediaType = image.getContentType();
            if (mediaType == null || mediaType.isEmpty()) {
                mediaType = determineMediaType(fileExtension);
            }

            // Формируем относительный путь для хранения в БД

            String relativePath = Paths.get(UPLOAD_DIR).relativize(filePath).toString();
            //String relativePath = Paths.get("uploads/").relativize(filePath).toString();

            // Создаем и возвращаем сущность изображения
            return new ImageEntity(relativePath, fileSize, mediaType);

        } catch (IOException e) {
            throw new RuntimeException("Не удалось сохранить изображение: " + e.getMessage(), e);
        }
    }


    /**
     * Получает сохранённое изображение по относительному пути к файлу.
     *
     * @param filePath относительный путь к файлу
     * @return массив байтов содержимого изображения
     */
    @Override
    @Transactional
    public byte[] getImage(String filePath) {
        try {
            // Безопасная проверка имени файла
            if (filePath == null || filePath.contains("..") || filePath.contains("/")) {
                throw new IllegalArgumentException("Некорректное имя файла");
            }

            // Ищем файл в разных директориях
            Path path = Paths.get(UPLOAD_DIR, filePath);


            if (Files.exists(path) && Files.isReadable(path)) {
                return Files.readAllBytes(path);
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
     * @param userId id пользователя
     * @param image  файл изображения аватара
     */
    @Override
    @Transactional
    public void uploadUserImage(Integer userId, MultipartFile image) {

        //Обновление и удаление старого файла
        UserEntity user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

        //Сохраняет старое изображение для удаления
        ImageEntity oldEntity = user.getImage();

        // Загружаем новое изображение
        ImageEntity newEntity = uploadImage(image, AVATARS_DIR, AVATAR_WIDTH, AVATAR_HEIGHT);

        // Сохраняем новое изображение в БД
        ImageEntity savedEntity = imageRepository.save(newEntity);

        // Обновляем связь в пользователе
        user.setImage(savedEntity);
        userRepository.save(user);

        // Удаляем старый файл
        delOldFile(oldEntity);
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
    @Transactional
    public ImageEntity uploadAdImage(Integer adId, MultipartFile image) {

        //Обновление и удаление старого файла
        AdEntity ad = adRepository.findById(adId).orElseThrow(() -> new IllegalArgumentException("Объявление не найдено"));

        // Сохраняем старое изображение для удаления
        ImageEntity oldEntity = ad.getImage();

        // Загружаем новое изображение
        ImageEntity newEntity = uploadImage(image, ADS_DIR, ADS_WIDTH, ADS_HEIGHT);

        // Сохраняем новое изображение в БД
        ImageEntity savedImage = imageRepository.save(newEntity);

        // Обновляем связь в объявлении
        ad.setImage(newEntity);
        adRepository.save(ad);

        // Удаляем старый файл
        delOldFile(oldEntity);

        return savedImage;
    }

    /**
     * Изменяет размер изображения до указанных размеров.
     * Если изображение меньше целевых размеров - изменения не производит.
     * Перезаписывает исходный файл.
     *
     * @param originalImage буферизованный файл
     * @param targetWidth   максимальная ширина
     * @param targetHeight  максимальная высота
     */
    private BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        // Определяем тип изображения (для поддержки прозрачности PNG)
        int type = originalImage.getTransparency() == Transparency.OPAQUE ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;

        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, type);
        Graphics2D g = resizedImage.createGraphics();

        // Настройка качества рендеринга
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Вычисляем масштабирование с сохранением пропорций
        double originalWidth = originalImage.getWidth();
        double originalHeight = originalImage.getHeight();

        double scaleX = targetWidth / originalWidth;
        double scaleY = targetHeight / originalHeight;
        double scale = Math.min(scaleX, scaleY);

        int scaledWidth = (int) (originalWidth * scale);
        int scaledHeight = (int) (originalHeight * scale);

        // Вычисляем координаты для центрирования
        int x = (targetWidth - scaledWidth) / 2;
        int y = (targetHeight - scaledHeight) / 2;

        // Если изображение не имеет прозрачности, заливаем фон
        if (originalImage.getTransparency() == Transparency.OPAQUE) {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, targetWidth, targetHeight);
        }

        // Рисуем масштабированное изображение
        g.drawImage(originalImage, x, y, scaledWidth, scaledHeight, null);
        g.dispose();

        return resizedImage;
    }

    /**
     * Определяет формат для ImageIO на основе расширения файла.
     */
    private String getFormatName(String extension) {
        return switch (extension.toLowerCase()) {
            case "jpg", "jpeg" -> "jpeg";
            case "png" -> "png";
            case "gif" -> "gif";
            case "bmp" -> "bmp";
            default -> "jpeg"; // По умолчанию
        };
    }

    /**
     * Определяет MIME-тип на основе расширения файла.
     */
    private String determineMediaType(String extension) {
        return switch (extension.toLowerCase()) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "bmp" -> "image/bmp";
            case "webp" -> "image/webp";
            default -> "application/octet-stream";
        };
    }

    /**
     * Удаляет файл по указанному пути с поддержкой относительных путей
     *
     * @param relativeFilePath относительный путь к файлу (например, "/uploads/avatar.jpg")
     * @return true - если файл успешно удалён, false - в случае ошибки
     */
    public boolean deleteFile(String relativeFilePath) {
        if (relativeFilePath == null || relativeFilePath.trim().isEmpty()) {
            log.debug("Путь к файлу пуст");
            return false;
        }

        try {
            // Получаем абсолютный путь (базовая директория задаётся в конфигурации)
            Path filePath = Paths.get(relativeFilePath).toAbsolutePath();

            File file = filePath.toFile();

            // Проверяем, существует ли файл и является ли он файлом (а не директорией)
            if (file.exists() && file.isFile()) {
                return file.delete();
            } else {
                System.out.println("Файл не существует или является директорией: " + filePath);
                return false;
            }
        } catch (Exception e) {
            log.error("Ошибка при удалении файла: {}", relativeFilePath, e);

            return false;
        }
    }

    public void delOldFile(ImageEntity imageEntity) {
        if (imageEntity == null) return;
        try {
            String filePath = imageEntity.getFilePath();
            if (filePath != null && !filePath.trim().isEmpty()) {
                // Полный путь к файлу
                Path fullPath = Paths.get(UPLOAD_DIR, filePath).normalize();

                // Удаляем файл
                deleteFile(fullPath.toString());

                // Удаляем запись из БД
                imageRepository.delete(imageEntity);
                log.debug("Старое изображение удалено: {}", filePath);
            }
        } catch (Exception e) {
            log.error("Ошибка при удалении старого файла", e);

        }
    }
}
