package ru.skypro.homework.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.skypro.homework.enums.Role;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

/**
 * Сущность пользователя системы.
 * Соответствует таблице 'users' в базе данных.
 *
 * <p>Связи:
 * <ul>
 *   <li>One-to-Many: пользователь может иметь несколько объявлений</li>
 *   <li>One-to-Many: пользователь может оставлять несколько комментариев</li>
 *   <li>One-to-One: пользователь может иметь один аватар</li>
 * </ul>
 *
 * @see ru.skypro.homework.dto.user.User
 * @see ru.skypro.homework.dto.reg.Register
 */
@Data
@NoArgsConstructor
@AllArgsConstructor

@Entity
@Table(name = "users")
public class UserEntity {

    /**
     * Уникальный идентификатор пользователя
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * Email пользователя (используется как логин)
     * <h4>Соответствие OpenAPI:</h4>
     * <li>В Register DTO: поле `username` (minLength=4, maxLength=32)</li>
     * <li>В User DTO: поле `email`</li>
     */
    @Column(nullable = false, unique = true, length = 32)
    @NotBlank(message = "Email не может быть пустым")
    @Size(min = 4, max = 32, message = "Email должен содержать от 4 до 32 символов")
    private String email;

    /**
     * Зашифрованный пароль пользователя
     *
     * <h4>Важно о безопасности:</h4>
     * Никогда не храним пароли в открытом виде!
     * Шифруем с помощью BCrypt (настраивается в WebSecurityConfig).
     * В БД хранится только хэш (например: "$2a$10$...").
     * <p>
     * Хэш BCrypt всегда фиксированной длины (60 символов).
     * Ограничения на пароль проверяются на уровне DTO (@Size(min=8, max=16)).
     * Здесь хранится уже зашифрованная версия.
     */
    @Column(nullable = false)
    @NotBlank(message = "Пароль не может быть пустым")
    private String password;

    /**
     * Имя пользователя
     */
    @Column(name = "first_name", nullable = false, length = 16)
    @NotBlank(message = "Имя не может быть пустым")
    @Size(min = 2, max = 16, message = "Имя должно содержать от 2 до 16 символов")
    private String firstName;

    /**
     * Фамилия пользователя
     */
    @Column(name = "last_name", nullable = false, length = 16)
    @NotBlank(message = "Фамилия не может быть пустым")
    @Size(min = 2, max = 16, message = "Фамилия должна содержать от 2 до 16 символов")
    private String lastName;

    /**
     * Номер телефона пользователя в формате +7 XXX XXX-XX-XX
     */
    @Column(nullable = false, length = 20)
    @NotBlank(message = "Телефон не может быть пустым")
    @Pattern(
            regexp = "\\+7\\s?\\(?\\d{3}\\)?\\s?\\d{3}-?\\d{2}-?\\d{2}",
            message = "Номер телефона должен соответствовать формату +7 XXX XXX-XX-XX"
    )
    private String phone;

    /**
     * Роль пользователя в системе
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;

    /**
     * Аватар пользователя
     */
    @OneToOne(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private ImageEntity image;

    /**
     * Список объявлений пользователя
     */
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AdEntity> ads = new ArrayList<>();

    /**
     * Список комментариев пользователя
     */
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommentEntity> comments = new ArrayList<>();

    /**
     * Флаг активности аккаунта
     */
    @Column(nullable = false)
    private boolean enabled = true;

    /**
     * Создает нового пользователя
     *
     * @param email     email пользователя
     * @param password  зашифрованный пароль
     * @param firstName имя
     * @param lastName  фамилия
     * @param phone     телефон
     * @param role      роль (по умолчанию USER)
     */
    public UserEntity(String email, String password, String firstName,
                      String lastName, String phone, Role role) {
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.role = role != null ? role : Role.USER;
        this.enabled = true;
    }

    /**
     * Возвращает полное имя пользователя
     *
     * @return полное имя в формате "Имя Фамилия"
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Проверяет, является ли пользователь администратором
     *
     * @return true если пользователь имеет роль ADMIN
     */
    public boolean isAdmin() {
        return Role.ADMIN.equals(this.role);
    }
}
