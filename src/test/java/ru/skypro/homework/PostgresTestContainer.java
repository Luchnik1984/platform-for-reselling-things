package ru.skypro.homework;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Утилитарный класс для управления PostgreSQL контейнером Testcontainers.
 * <p>
 * Реализует Singleton паттерн для переиспользования одного контейнера
 * между всеми интеграционными тестами.
 * </p>
 */
public class PostgresTestContainer {
    private static PostgreSQLContainer<?> container;

    /**
     * Возвращает экземпляр PostgreSQL контейнера.
     * <p>
     * При первом вызове создает и запускает контейнер.
     * При последующих вызовах возвращает уже запущенный контейнер.
     * </p>
     *
     * @return запущенный PostgreSQL контейнер
     */
    public static PostgreSQLContainer<?> getInstance() {
        if (container == null) {

            container = new PostgreSQLContainer<>( DockerImageName.parse("postgres:15-alpine"))
                    .withDatabaseName("testdb")
                    .withUsername("testuser")
                    .withPassword("testpass")
                    .withReuse(false);  // Отключение переиспользования

            container.start();

            /// Регистрируем shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(container::stop));
        }
        return container;
    }

    /**
     * Приватный конструктор для предотвращения создания экземпляров класса.
     * <p>
     * Класс является утилитарным и должен использоваться только через
     * статический метод {@link #getInstance()}.
     * </p>
     */
    private PostgresTestContainer()  {
        throw new UnsupportedOperationException("Utility class - cannot be instantiated");
    }
}

