package ru.skypro.homework.service.integration;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import ru.skypro.homework.entity.UserEntity;
import ru.skypro.homework.enums.Role;

import java.util.Collections;
import java.util.List;

/**
 * Вспомогательные методы для создания тестовых аутентификаций.
 * Гарантирует согласованность между тестовыми классами.
 */
public class TestAuthenticationUtils {

    /**
     * Создает объект {@link Authentication} для тестового пользователя.
     * <p>
     * Для ADMIN включает обе роли: ROLE_ADMIN и ROLE_USER.
     * Для USER включает только ROLE_USER.
     *
     * @param userEntity сущность пользователя
     * @return объект аутентификации
     */
    public static Authentication createAuthentication(UserEntity userEntity) {
        List<SimpleGrantedAuthority> authorities;

        if (userEntity.getRole() == Role.ADMIN) {
            authorities = List.of(
                    new SimpleGrantedAuthority("ROLE_ADMIN"),
                    new SimpleGrantedAuthority("ROLE_USER")
            );
        } else {
            authorities = Collections.singletonList(
                    new SimpleGrantedAuthority("ROLE_USER")
            );
        }

        return new UsernamePasswordAuthenticationToken(
                userEntity.getEmail(),
                "testpassword",  // единый тестовый пароль
                authorities
        );
    }

    /**
     * Создает объект {@link Authentication} по данным пользователя.
     *
     * @param email email пользователя
     * @param role роль пользователя
     * @return объект аутентификации
     */
    public static Authentication createAuthentication(String email, Role role) {
        List<SimpleGrantedAuthority> authorities;

        if (role == Role.ADMIN) {
            authorities = List.of(
                    new SimpleGrantedAuthority("ROLE_ADMIN"),
                    new SimpleGrantedAuthority("ROLE_USER")
            );
        } else {
            authorities = Collections.singletonList(
                    new SimpleGrantedAuthority("ROLE_USER")
            );
        }

        return new UsernamePasswordAuthenticationToken(email, "testpassword", authorities);
    }
}
