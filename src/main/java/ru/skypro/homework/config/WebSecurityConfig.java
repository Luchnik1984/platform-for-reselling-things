package ru.skypro.homework.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import ru.skypro.homework.enums.Role;

import java.util.Arrays;
import java.util.List;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * Конфигурация безопасности Spring Security.
 * Настраивает Basic Authentication, CORS и ролевой доступ.
 *
 * <p>Основные функции:
 * <ul>
 *   <li>Basic Authentication для REST API</li>
 *   <li>CORS для фронтенда на порту 3000 (или кастомном)</li>
 *   <li>Разграничение доступа по ролям USER/ADMIN</li>
 *   <li>Тестовые пользователи в памяти</li>
 * </ul>
 */
@Configuration
public class WebSecurityConfig {
    /**
     * Порт фронтенда для CORS.
     * По умолчанию 3000, можно изменить через переменную окружения FRONTEND_PORT.
     */
    @Value("${FRONTEND_PORT:3000}")
    private String frontendPort;

    /**
     * Белый список эндпоинтов, не требующих аутентификации.
     */
    private static final String[] AUTH_WHITELIST = {
            "/swagger-resources/**",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/v3/api-docs.yaml",
            "/api-docs/**",
            "/api-docs.yaml",
            "/webjars/**",
            "/login",
            "/register",
            "/images/**",
            "/error"
    };

    /**
     * Создаёт тестовых пользователей в памяти.
     * Используется для тестирования на Этапе 1-2.
     * На Этапе 3 будет заменён на UserDetailsService из БД.
     */
    @Bean
    public InMemoryUserDetailsManager userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails user =
                User.builder()
                        .username("user@gmail.com")
                        .password("password")
                        .passwordEncoder(passwordEncoder::encode)
                        .roles(Role.USER.name())
                        .build();

        UserDetails admin =
                User.builder()
                        .username("admin@gmail.com")
                        .password("admin")
                        .passwordEncoder(passwordEncoder::encode)
                        .roles(Role.ADMIN.name())
                        .build();
        return new InMemoryUserDetailsManager(user,admin);
    }

    /**
     * Основная цепочка фильтров безопасности.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf()
                .disable()
                .cors().configurationSource(corsConfigurationSource())
                .and()
                .authorizeHttpRequests(
                        authorization ->
                                authorization
                                        .mvcMatchers(AUTH_WHITELIST)
                                        .permitAll()
                                        .mvcMatchers("/ads/**", "/users/**")
                                        .authenticated()
                                        .mvcMatchers("/admin/**").hasRole(Role.ADMIN.name())
                )
                .httpBasic(withDefaults());
        return http.build();
    }

    /**
     * Конфигурация CORS для фронтенда.
     * Разрешает запросы с указанного порта фронтенда.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Разрешаем запросы с фронтенда
        String frontendOrigin = "http://localhost:" + frontendPort;
        configuration.setAllowedOrigins(List.of(frontendOrigin));

        // Разрешаем стандартные HTTP методы
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));

        // Разрешаем все заголовки
        configuration.setAllowedHeaders(List.of("*"));

        // Разрешаем credentials (куки, авторизация)
        configuration.setAllowCredentials(true);

        // Кэшируем preflight запросы на 1 час
        configuration.setMaxAge(3600L);

        // Применяем ко всем эндпоинтам
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    /**
     * Кодировщик паролей (BCrypt).
     * Используется для хеширования паролей перед сохранением в БД.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
