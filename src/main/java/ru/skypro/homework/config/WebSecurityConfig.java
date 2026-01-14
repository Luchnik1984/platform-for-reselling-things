package ru.skypro.homework.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.List;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * Конфигурация безопасности Spring Security.
 * Настраивает Basic Authentication, CORS и ролевой доступ.
 *
 * <p>Основные функции:
 * <ul>
 *   <li>Реализация JdbcUserDetailsManager + AuthenticationManager.</li>
 *   <li>CORS для фронтенда на порту 3000 (или кастомном)</li>
 *   <li><strong>Разделение доступа:</strong>
 *       {@code GET /ads} - публичный. Остальные операции - с проверкой ролей</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
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
     * JdbcUserDetailsManager - служба управления пользователями.
     * Реализует UserDetailsService для приложения.
     * Загружает пользователей из БД.
     *
     * <p>Конфигурация:
     * <ol>
     * <li> Явно настраивается с DataSource через .dataSource(dataSource)</li>
     * <li> Указывает SQL запросы для загрузки пользователей и authorities</li>
     * <li> Использует схему БД, созданную миграциями Flyway</li>
     * </ol>
     *
     * <p>SQL запросы:
     * <ul>
     * <li> usersByUsernameQuery: загружает username, password, enabled</li>
     * <li> authoritiesByUsernameQuery: загружает username и authority (ROLE_*)</li>
     *  </ul>
     *
     * @param dataSource DataSource для подключения к БД
     * @return настроенный JdbcUserDetailsManager
     */
    @Bean
    public JdbcUserDetailsManager userDetailsManager(DataSource dataSource) {
        JdbcUserDetailsManager manager = new JdbcUserDetailsManager();
        manager.setDataSource(dataSource);

        manager.setUsersByUsernameQuery(
                "SELECT email AS username, password, enabled " +
                        "FROM users " +
                        "WHERE email = ?"
        );

        manager.setAuthoritiesByUsernameQuery(
                "SELECT email AS username, 'ROLE_' || role AS authority " +
                        "FROM users " +
                        "WHERE email = ?"
        );

        return manager;
    }


    /**
     * Основная цепочка фильтров безопасности.
     * <ol>
     * <li> GET /ads доступен без аутентификации</li>
     * <li> PUT, DELETE, POST требуют роли USER или ADMIN</li>
     * </ol>
     *
     * @param http объект для настройки безопасности HTTP
     * @return сконфигурированная цепочка фильтров
     * @throws Exception если конфигурация не удалась
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

                                        // GET /ads доступен всем
                                        .mvcMatchers(HttpMethod.GET, "/ads")
                                        .permitAll()

                                        // Остальные запросы требуют аутентификации
                                        .mvcMatchers("/ads/**", "/users/**", "/comments/**")
                                        .authenticated()
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


    /**
     * AuthenticationManager для использования в сервисах.
     *
     * <p>Для чего нужен:
     * <ol></ol>
     * <li> AuthServiceImpl использует его для проверки паролей в методе login()</li>
     * <li> Spring Security использует его для Basic Authentication</li>
     * </ol>
     *
     * @param authConfig конфигурация аутентификации Spring Security
     * @return сконфигурированный AuthenticationManager
     * @throws Exception если конфигурация не удалась
     */
    @Bean
    public AuthenticationManager authenticationManagerBean(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

}
