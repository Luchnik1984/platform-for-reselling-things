package ru.skypro.homework.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * Сущность комментария к объявлению.
 * Соответствует таблице 'comments' в базе данных.
 *
 * <p>Связи:
 * <ul>
 *   <li>Many-to-One: комментарий принадлежит одному пользователю (автору)</li>
 *   <li>Many-to-One: комментарий относится к одному объявлению</li>
 * </ul>
 *
 * @see ru.skypro.homework.dto.comments.Comment
 * @see ru.skypro.homework.dto.comments.CreateOrUpdateComment
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "comments")
public class CommentEntity {

    /**
     * Уникальный идентификатор комментария
     * соответствует pk в DTO Comment
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * Текст комментария
     */
    @Column(nullable = false, length = 64)
    @NotBlank(message = "Текст комментария не может быть пустым")
    @Size(min = 8, max = 64, message = "Текст комментария должен содержать от 8 до 64 символов")
    private String text;

    /**
     * Дата и время создания комментария
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Автор комментария
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    @NotNull(message = "Автор не может быть null")
    private UserEntity author;

    /**
     * Объявление, к которому относится комментарий
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ad_id", nullable = false)
    @NotNull(message = "Объявление не может быть null")
    private AdEntity ad;

    /**
     * Создает новый комментарий
     *
     * @param text   текст комментария
     * @param author автор комментария
     * @param ad     объявление, к которому относится комментарий
     */
    public CommentEntity(String text, UserEntity author, AdEntity ad) {
        this.text = text;
        this.author = author;
        this.ad = ad;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Проверяет, принадлежит ли комментарий указанному пользователю
     *
     * @param userId идентификатор пользователя
     * @return true если пользователь является автором комментария
     */
    public boolean isAuthor(Integer userId) {
        return author != null && author.getId().equals(userId);
    }

}
