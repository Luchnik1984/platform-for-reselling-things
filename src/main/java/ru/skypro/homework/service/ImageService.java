package ru.skypro.homework.service;

import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.entity.ImageEntity;

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
public interface ImageService {

    /**
     * Сохраняет переданный файл изображения в файловой системе.
     * Генерирует уникальное имя файла для избежания коллизий.
     *
     * @param image           загружаемый файл изображения
     * @param imageUploadPath путь, куда сохранять файл
     * @param width           ширина изображения
     * @param height          высота изображения
     * @return ImageEntity для записи его в базу данных
     */
    ImageEntity uploadImage(MultipartFile image, String imageUploadPath, int width, int height);


    /**
     * Получает сохранённое изображение по относительному пути к файлу.
     *
     * @param path относительный путь к файлу
     * @return массив байтов содержимого изображения
     */
    byte[] getImage(String path);

    /**
     * Сохраняет аватар (изображение профиля) для конкретного пользователя.
     * Автоматически удаляет предыдущий аватар пользователя, если он существует.
     *
     * @param userId идентификатор пользователя
     * @param image  файл изображения аватара
     * @return Измененный user
     */
    void uploadUserImage(Integer userId, MultipartFile image);

    /**
     * Сохраняет изображение для конкретного объявления.
     * Автоматически удаляет предыдущие изображения этого объявления при необходимости.
     *
     * @param adId  идентификатор объявления
     * @param image файл изображения объявления
     * @return относительный путь к сохранённому изображению (например, "/ads/ad_456.jpg")
     */
    String uploadAdImage(Integer adId, MultipartFile image);
}
