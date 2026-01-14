package ru.skypro.homework;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Базовый класс для интеграционных тестов с использованием Testcontainers.
 * <p>
 * Предоставляет готовый PostgreSQL контейнер для всех наследующих классов.
 * Контейнер запускается один раз при загрузке класса и автоматически останавливается
 * через JVM shutdown hook после выполнения всех тестов.
 * </p>
 *
 * @see Testcontainers
 * @see PostgreSQLContainer
 */
@SpringBootTest
@Testcontainers
@ActiveProfiles("docker-test")
public abstract class AbstractIntegrationTest {

    /**
     * PostgreSQL контейнер для тестов.
     * Используется всеми интеграционными тестами.
     */
     static final PostgreSQLContainer<?> POSTGRES;

    ///Статический блок инициализации контейнера PostgreSQL. Выполняется один раз при первой загрузке класса в JVM.
    static {
        POSTGRES = PostgresTestContainer.getInstance();
    }

    /**
     * Динамически регистрирует свойства Spring для подключения к контейнеру PostgreSQL.
     * <p>
     * Метод вызывается Spring Framework перед созданием тестового контекста
     * и переопределяет стандартные настройки DataSource на параметры Testcontainers.
     * <p>
     *  @param registry реестр для динамической регистрации свойств.
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);

        registry.add("spring.flyway.enabled", () -> true);
        registry.add("spring.flyway.locations", () -> "classpath:db/migration");
        registry.add("spring.flyway.baseline-on-migrate", () -> true);

    }
    /**
     * Конструктор для предотвращения прямого создания экземпляра.
     */
    protected AbstractIntegrationTest() {}

}
