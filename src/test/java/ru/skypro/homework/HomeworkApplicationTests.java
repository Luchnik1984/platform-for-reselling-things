package ru.skypro.homework;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Базовый тест для проверки загрузки контекста Spring Boot приложения.
 * <p>
 * Использует профиль 'test' с H2 in-memory БД для быстрого выполнения.
 * Этот тест проверяет корректность конфигурации Spring Beans и отсутствие
 * циклических зависимостей при запуске приложения.
 *
 * <p><b>Особенности:</b>
 * <ul>
 *   <li>Использует H2 in-memory БД (быстро, не требует Docker)</li>
 *   <li>Профиль 'test' изолирует от production-настроек</li>
 *   <li>Не выполняет бизнес-лоику, только проверяет контекст</li>
 * </ul>
 * @see AbstractIntegrationTest для интеграционных тестов с реальной БД
 * @see org.springframework.boot.test.context.SpringBootTest
 */
@SpringBootTest
@ActiveProfiles("test")
class HomeworkApplicationTests {

    /**
     * Проверяет успешную загрузку контекста Spring Boot приложения.
     * <p>
     * Если контекст не загружается, тест завершится с ошибкой, указывая
     * на проблемы в конфигурации приложения.
     * <p>
     * Успешное выполнение метода означает, что контекст Spring загружен корректно.
     */
    @Test
    void contextLoads() {
    }

}
