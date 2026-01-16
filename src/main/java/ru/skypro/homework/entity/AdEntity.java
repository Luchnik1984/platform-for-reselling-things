package ru.skypro.homework.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Сущность объявления о продаже.
 * Соответствует таблице 'ads' в базе данных.
 *
 * <p>Связи:
 * <ul>
 *   <li>Many-to-One: объявление принадлежит одному пользователю (автору)</li>
 *   <li>One-to-Many: объявление может иметь несколько комментариев</li>
 *   <li>One-to-One: объявление может иметь одно изображение</li>
 * </ul>
 *
 * @see ru.skypro.homework.dto.ads.Ad
 * @see ru.skypro.homework.dto.ads.ExtendedAd
 * @see ru.skypro.homework.dto.ads.CreateOrUpdateAd
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ads")
public class AdEntity {

    /**
     * Уникальный идентификатор объявления
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * Заголовок объявления
     */
    @Column(nullable = false, length = 32)
    @NotBlank(message = "Заголовок не может быть пустым")
    @Size(min = 4, max = 32, message = "Заголовок должен содержать от 4 до 32 символов")
    private String title;

    /**
     * Цена товара в рублях
     */
    @Column(nullable = false)
    @NotNull(message = "Цена не может быть null")
    @Min(value = 0, message = "Цена не может быть отрицательной")
    private Integer price;

    /**
     * Описание товара
     */
    @Column(nullable = false, length = 64)
    @NotBlank(message = "Описание не может быть пустым")
    @Size(min = 8, max = 64, message = "Описание должно содержать от 8 до 64 символов")
    private String description;

    /**
     * Дата и время создания объявления
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Автор объявления
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    @NotNull(message = "Автор не может быть null")
    @ToString.Exclude
    private UserEntity author;

    /**
     * Изображение объявления
     */
    @OneToOne(cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @ToString.Exclude
    @JoinColumn(name = "image_id")
    private ImageEntity image;

    /**
     * Комментарии к объявлению
     */
    @OneToMany(mappedBy = "ad", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<CommentEntity> comments = new ArrayList<>();

    /**
     * Создает новое объявление
     *
     * @param title       заголовок
     * @param price       цена
     * @param description описание
     * @param author      автор
     */
    public AdEntity(String title, Integer price, String description, UserEntity author) {
        this.title = title;
        this.price = price;
        this.description = description;
        this.author = author;
        this.createdAt = LocalDateTime.now();
    }

}
