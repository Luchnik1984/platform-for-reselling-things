package ru.skypro.homework.mapper;

import ru.skypro.homework.dto.reg.Register;
import ru.skypro.homework.dto.user.User;
import ru.skypro.homework.entity.UserEntity;

/**
 * Маппер для преобразования между сущностью User и DTO.
 *
 * <p>Преобразования:
 * <ul>
 *   <li>{@link Register} → {@link UserEntity} (при регистрации)</li>
 *   <li>{@link UserEntity} → {@link User} (при получении профиля)</li>
 *   <li>Обновление {@link UserEntity} из {@link Register} (при изменении профиля)</li>
 * </ul>
 *
 * @see UserEntity
 * @see Register
 * @see User
 */
public interface UserMapper {

    /**
     * Преобразует DTO регистрации в сущность User.
     *
     * <p>Особенности:
     * <ul>
     *   <li>Поле {@code username} в Register → {@code email} в UserEntity</li>
     *   <li>Пароль должен быть уже зашифрован (хеширован) до вызова этого метода</li>
     *   <li>Роль по умолчанию - USER (если не указана)</li>
     * </ul>
     *
     * @param register DTO регистрации
     * @return сущность пользователя
     */

    UserEntity toEntity(Register register);

    /**
     * Преобразует сущность User в DTO для ответа.
     *
     * <p>Особенности:
     * <ul>
     *   <li>Поле {@code email} в UserEntity → {@code email} в User (то же имя)</li>
     *   <li>Если есть аватар ({@code image}), преобразует в URL строку</li>
     *   <li>Не включает пароль (никогда не отправляем пароль в ответах)</li>
     * </ul>
     *
     * @param userEntity сущность пользователя
     * @return DTO пользователя
     */

    User toDto(UserEntity userEntity);

    /**
     * Обновляет сущность пользователя из DTO регистрации.
     * Используется для частичного обновления профиля.
     *
     * <p>Особенности:
     * <ul>
     *   <li>Обновляет только не-null поля из source</li>
     *   <li>Не обновляет пароль (для смены пароля отдельный метод)</li>
     *   <li>Не обновляет email (логин) - он неизменяем</li>
     * </ul>
     *
     * @param updateDto DTO с обновлениями
     * @param userEntity сущность для обновления
     */

    void updateEntityFromRegister(Register updateDto, UserEntity userEntity);

}
