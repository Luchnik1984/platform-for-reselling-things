package ru.skypro.homework.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

/**
 * Сущность для хранения информации об изображениях.
 * Соответствует таблице 'images' в базе данных.
 *
 * <p>Особенности:
 * <ul>
 *   <li>Хранит не сами файлы, а метаданные и путь к файлу</li>
 *   <li>Связана с UserEntity (аватар пользователя) и AdEntity (изображение объявления)</li>
 *   <li>Файлы хранятся в файловой системе, пути - в базе данных</li>
 * </ul>
 *
 * <p>Файлы хранятся в файловой системе по пути, указанному в {@code filePath}.
 * Для доступа к изображению используется URL, возвращаемый методом {@link #getImageUrl()}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "images")
public class ImageEntity {

    /**
     * Уникальный идентификатор изображения
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * Путь к файлу изображения в файловой системе
     * Пример: "/uploads/images/avatars/user-5-avatar.jpg"
     * <p>Особенности:
     * <ul>
     *   <li>Путь должен быть уникальным для каждого файла</li>
     *   <li>Не должен начинаться с "/" (корректируется в getImageUrl())</li>
     *   <li>Файлы хранятся локально, в продакшене можно перенести в облако</li>
     * </ul>
     */
    @Column(name = "file_path", nullable = false, unique = true)
    @NotBlank(message = "Путь к файлу не может быть пустым")
    private String filePath;

    /**
     * Размер файла в байтах
     */
    @Column(name = "file_size", nullable = false)
    @NotNull(message = "Размер файла не может быть null")
    @Positive(message = "Размер файла должен быть положительным")
    private Long fileSize;

    /**
     * MIME-тип файла
     * Пример: "image/jpeg", "image/png", "image/gif"
     */
    @Column(name = "media_type", nullable = false, length = 50)
    @NotBlank(message = "MIME-тип не может быть пустым")
    private String mediaType;


    /**
     * Возвращает URL для доступа к изображению через API.
     *
     * <p>Логика работы:
     * <ol>
     *   <li>Если filePath начинается с "/" - убираем его</li>
     *   <li>Добавляем префикс "/images/" для доступа через ImageController</li>
     *   <li>Пример: "uploads/ads/123.jpg" → "/images/uploads/ads/123.jpg"</li>
     * </ol>
     *
     * <p>Соответствие OpenAPI:
     * - В DTO User: поле {@code image} должно содержать URL типа String
     * - В DTO Ad: поле {@code image} должно содержать URL типа String
     * - В DTO Comment: поле {@code authorImage} должно содержать URL типа String
     *
     * @return полный URL для доступа к изображению через веб-интерфейс
     */
    public String getImageUrl() {
        if (filePath == null || filePath.isEmpty()) {
            return null;
        }
        // Префикс "/images/" будет обрабатываться ImageController
        return "/images/" + filePath.replace('\\', '/');
    }

    /**
     * Проверяет, является ли изображение аватаром пользователя.
     * В текущей реализации ImageEntity не хранит прямую ссылку на пользователя,
     * поэтому всегда возвращает false. Определение типа изображения
     * выполняется на уровне сервиса через проверку связей.
     *
     * @return false (в этой реализации)
     * @deprecated Используйте проверку через UserRepository или AdRepository
     */
    @Deprecated
    public boolean isUserAvatar() {
        return false;
    }

    /**
     * Проверяет, является ли изображение картинкой объявления.
     * Аналогично isUserAvatar(), определение типа выполняется через сервис.
     *
     * @return false (в этой реализации)
     * @deprecated Используйте проверку через UserRepository или AdRepository
     */
    @Deprecated
    public boolean isAdImage() {
        return false;
    }

    /**
     * Создает изображение с указанными метаданными.
     * Конструктор для ручного создания (например, в тестах).
     *
     * @param filePath  путь к файлу (не начинать с "/")
     * @param fileSize  размер файла в байтах
     * @param mediaType MIME-тип файла
     */
    public ImageEntity(String filePath, Long fileSize, String mediaType) {
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.mediaType = mediaType;
    }
}
