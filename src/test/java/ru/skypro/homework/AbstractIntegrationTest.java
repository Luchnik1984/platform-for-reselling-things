package ru.skypro.homework;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Абстрактный базовый класс для всех интеграционных тестов с использованием Testcontainers.
 * <p>
 * Предоставляет готовый контейнер PostgreSQL для тестов, требующих реальной базы данных.
 * Контейнер запускается один раз на всю сессию тестирования и переиспользуется между тестами.
 *
 * <p><b>Особенности реализации:</b>
 * <ul>
 *   <li>Использует PostgreSQL 15 Alpine образ для минимального потребления памяти</li>
 *   <li>Контейнер запускается в статическом блоке для однократной инициализации</li>
 *   <li>Автоматически применяет миграции Flyway при запуске</li>
 *   <li>Поддерживает переиспользование контейнера между тестами (withReuse=true)</li>
 *   <li>Использует профиль 'docker-test' для изоляции от других тестов</li>
 * </ul>
 *
 * <p><b>Как использовать:</b>
 * <ol>
 *   <li>Наследовать тестовый класс от AbstractIntegrationTest</li>
 *   <li>Аннотировать класс {@code @SpringBootTest} (уже есть в родителе)</li>
 *   <li>Использовать {@code @Autowired} для инъекции зависимостей</li>
 * </ol>
 *
 * <p><b>Пример:</b>
 * <pre>{@code
 * class UserRepositoryIntegrationTest extends AbstractIntegrationTest {
 *     @Autowired
 *     private UserRepository userRepository;
 *
 *     @Test
 *     void testFindByEmail() {
 *         // тест работает с реальной PostgreSQL в Docker
 *     }
 * }
 * }</pre>
 *
 * <p><b>Профили тестирования:</b>
 * <table border="1">
 *   <tr><th>Профиль</th><th>База данных</th><th>Назначение</th></tr>
 *   <tr><td>test</td><td>H2 in-memory</td><td>Unit-тесты (быстрые)</td></tr>
 *   <tr><td>docker-test</td><td>PostgreSQL в Docker</td><td>Интеграционные тесты</td></tr>
 * </table>
 */
@SpringBootTest
@Testcontainers
@ActiveProfiles("docker-test")
public abstract class AbstractIntegrationTest {

    /**
     * Docker-контейнер с PostgreSQL для интеграционных тестов.
     * <p>
     * <b>Конфигурация контейнера:</b>
     * <ul>
     *   <li>Образ: postgres:15-alpine </li>
     *   <li>Имя БД: testdb</li>
     *   <li>Пользователь: testuser</li>
     *   <li>Пароль: testpass</li>
     *   <li>Переиспользование: включено (withReuse=true)</li>
     * </ul>
     *
     * <p><b>Важно:</b> Контейнер объявлен как {@code static final}, что обеспечивает:
     * <ul>
     *   <li>Запуск один раз на всю JVM сессию</li>
     *   <li>Переиспользование между тестовыми классами</li>
     *   <li>Корректное завершение после всех тестов</li>
     * </ul>
     * <p><b> Статический блок</b> инициализации контейнера PostgreSQL.
     * <p>
     * Выполняется при первой загрузке класса в JVM и обеспечивает:
     * <ol>
     *   <li>Создание и настройку контейнера</li>
     *   <li>Запуск контейнера до начала любых тестов</li>
     *   <li>Ожидание полной инициализации PostgreSQL</li>
     *   <li>Обработку возможных исключений при запуске</li>
     * </ol>
     *
     * <p><b>Примечание:</b> Используется {@code Thread.sleep(2000)} для гарантии,
     * что PostgreSQL полностью инициализирован и готов принимать подключения.
     * RuntimeException если не удалось запустить контейнер или дождаться его готовности.
     */
    @Container
     static final PostgreSQLContainer<?> POSTGRES;

    static {

        POSTGRES = new PostgreSQLContainer<>(DockerImageName.parse("postgres:15-alpine"))
                .withDatabaseName("testdb")
                .withUsername("testuser")
                .withPassword("testpass")
                .withReuse(true);

        POSTGRES.start();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Не удалось дождаться инициализации PostgreSQL", e);
        }
    }

    /**
     * Динамически регистрирует свойства Spring для подключения к контейнеру PostgreSQL.
     * <p>
     * Метод вызывается Spring Framework перед созданием тестового контекста
     * и переопределяет настройки базы данных из application-docker-test.properties.
     * <p>
     * Гарантирует, что Flyway включен и будет применять миграции.
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
     * <p>
     * Класс предназначен только для наследования тестовыми классами.
     */
    protected AbstractIntegrationTest() {}

}
