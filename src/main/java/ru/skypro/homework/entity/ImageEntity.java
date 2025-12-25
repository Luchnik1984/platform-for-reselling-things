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
     * Пользователь, которому принадлежит это изображение (аватар)
     * Может быть null если изображение относится к объявлению
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    /**
     * Объявление, которому принадлежит это изображение
     * Может быть null если изображение является аватаром пользователя
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ad_id")
    private AdEntity ad;

    /**
     * Создает изображение для пользователя (аватар)
     *
     * @param filePath  путь к файлу
     * @param fileSize  размер файла
     * @param mediaType MIME-тип
     * @param user      пользователь
     */
    public ImageEntity(String filePath, Long fileSize, String mediaType, UserEntity user) {
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.mediaType = mediaType;
        this.user = user;
        this.ad = null;
    }

    /**
     * Создает изображение для объявления
     *
     * @param filePath  путь к файлу
     * @param fileSize  размер файла
     * @param mediaType MIME-тип
     * @param ad        объявление
     */
    public ImageEntity(String filePath, Long fileSize, String mediaType, AdEntity ad) {
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.mediaType = mediaType;
        this.user = null;
        this.ad = ad;
    }

    /**
     * Возвращает URL для доступа к изображению через API
     *
     * @return URL изображения
     */
    public String getImageUrl() {
        // В продакшене здесь будет полный URL
        // Пока возвращаем путь, который будет обслуживаться контроллером
        return "/images" + filePath;
    }

    /**
     * Проверяет, является ли изображение аватаром пользователя
     *
     * @return true если изображение связано с пользователем
     */
    public boolean isUserAvatar() {
        return user != null;
    }

    /**
     * Проверяет, является ли изображение картинкой объявления
     *
     * @return true если изображение связано с объявлением
     */
    public boolean isAdImage() {
        return ad != null;
    }
}
