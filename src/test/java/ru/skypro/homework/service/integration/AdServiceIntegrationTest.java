package ru.skypro.homework.service.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import ru.skypro.homework.AbstractIntegrationTest;
import ru.skypro.homework.dto.ads.Ad;
import ru.skypro.homework.dto.ads.Ads;
import ru.skypro.homework.dto.ads.CreateOrUpdateAd;
import ru.skypro.homework.dto.ads.ExtendedAd;
import ru.skypro.homework.entity.AdEntity;
import ru.skypro.homework.entity.UserEntity;
import ru.skypro.homework.enums.Role;
import ru.skypro.homework.exceptions.AccessDeniedException;
import ru.skypro.homework.exceptions.AdNotFoundException;
import ru.skypro.homework.exceptions.GlobalExceptionHandler;
import ru.skypro.homework.repository.AdRepository;
import ru.skypro.homework.repository.UserRepository;
import ru.skypro.homework.service.AdService;
import ru.skypro.homework.service.impl.AdServiceImpl;
import ru.skypro.homework.util.SecurityUtils;
import ru.skypro.homework.util.TestAuthenticationUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Интеграционные тесты для {@link AdService} с использованием Testcontainers.
 * <p>
 * Тестирует бизнес-логику работы с объявлениями, с полным контекстом Spring
 * и изолированной PostgreSQL в Docker.
 *
 * <p><b>Основные тестируемые сценарии:</b>
 * <ul>
 *   <li>Получение всех объявлений и фильтрация по автору</li>
 *   <li>Создание, обновление и удаление объявлений</li>
 *   <li>Проверка прав доступа (USER/ADMIN)</li>
 *   <li>Обработка исключений для несуществующих ресурсов</li>
 * </ul>
 *
 * @see AdService
 * @see AdServiceImpl
 * @see AbstractIntegrationTest
 */
@Tag("integration")
@Transactional
@DisplayName("Интеграционные тесты AdService с Testcontainers")
class AdServiceIntegrationTest extends AbstractIntegrationTest {

    /**
     * Тестируемый сервис объявлений.
     * <p>
     * Инжектится Spring из полного контекста приложения.
     * Содержит всю бизнес-логику работы с объявлениями,
     * включая проверку прав доступа через {@link SecurityUtils}.
     */
    @Autowired
    private AdService adService;

    /**
     * Репозиторий объявлений для подготовки тестовых данных.
     * <p>
     * Используется для:
     * <ul>
     *   <li>Создания тестовых объявлений перед выполнением тестов</li>
     *   <li>Проверки состояния БД после операций сервиса</li>
     *   <li>Получения ID созданных сущностей для assertions</li>
     * </ul>
     */
    @Autowired
    private AdRepository adRepository;

    /**
     * Репозиторий пользователей для создания тестовых авторов.
     * <p>
     * Необходим для создания пользователей с разными ролями (USER/ADMIN)
     * и проверки ролевой модели доступа.
     */
    @Autowired
    private UserRepository userRepository;

    /**
     * Кодировщик паролей для создания тестовых пользователей.
     * <p>
     * Используется тот же PasswordEncoder (BCrypt), что и в основном приложении,
     * для корректной работы Spring Security.
     */
    @Autowired
    private PasswordEncoder passwordEncoder;

    private UserEntity user1;

    private UserEntity user2;

    private UserEntity adminUser;

    private Authentication user1Auth;

    private Authentication user2Auth;

    private Authentication adminUserAuth;

    /**
     * Идентификатор сохраненного тестового объявления.
     * <p>
     * Сохраняется после создания тестовых данных для использования
     * в тестах, требующих конкретный ID объявления.
     */
    private Integer savedAdId;


    /**
     * Подготовка тестовых данных перед каждым тестом.
     * <p>
     * <b>Выполняет:</b>
     * <ol>
     *   <li>Очистку таблиц через каскадное удаление</li>
     *   <li>Создание трех тестовых пользователей (два USER, один ADMIN)</li>
     *   <li>Создание нескольких тестовых объявлений от разных авторов</li>
     *   <li>Сохранение ID одного из объявлений для последующего использования</li>
     * </ol>
     *
     * <p><b>Транзакционность:</b> Метод выполняется в транзакции, которая
     * автоматически откатывается после каждого теста благодаря {@code @Transactional}
     * в {@link AbstractIntegrationTest}.
     */
    @BeforeEach
    void setUp() {
        // Очищаем таблицы (каскадно через связи)
        adRepository.deleteAll();
        userRepository.deleteAll();

        // Создаём тестовых пользователей с хешированными паролями
        user1 = createTestUser(
                "user1@example.com",
                "Иван",
                "Иванов",
                Role.USER);
        user2 = createTestUser(
                "user2@example.com",
                "Петр",
                "Петров",
                Role.USER);
        adminUser = createTestUser(
                "admin@example.com",
                "Админ",
                "Админов", Role.ADMIN);

        // Создаём тестовые объявления
        AdEntity ad1 = new AdEntity(
                "Велосипед горный",
                15000,
                "Отличный горный велосипед, почти новый",
                user1
        );

        AdEntity ad2 = new AdEntity(
                "Ноутбук игровой",
                75000,
                "Игровой ноутбук с RTX 3060, 1 год использования",
                user1
        );

        AdEntity ad3 = new AdEntity(
                "Книги по программированию",
                2500,
                "Коллекция книг: Clean Code, Effective Java, Patterns",
                user2
        );

        // Сохраняем объявления и запоминаем ID третьего объявления
        adRepository.save(ad1);
        adRepository.save(ad2);
        AdEntity savedAd3 = adRepository.save(ad3);
        savedAdId = savedAd3.getId();

        user1Auth = TestAuthenticationUtils.createAuthentication(user1);
        adminUserAuth = TestAuthenticationUtils.createAuthentication(adminUser);
        user2Auth = TestAuthenticationUtils.createAuthentication(user2);

    }

    /**
     * Тестирует получение списка всех объявлений.
     * <p>
     * <b>Сценарий:</b> Аутентифицированный пользователь запрашивает все объявления.
     * <p>
     * <b>Проверяемые аспекты:</b>
     * <ul>
     *   <li>Корректность общего количества объявлений</li>
     *   <li>Наличие всех созданных объявлений в результате</li>
     *   <li>Корректность структуры DTO (поля pk, author, title, price, image)</li>
     *   <li>Null-безопасность для поля image (изображения отсутствуют)</li>
     * </ul>
     */
    @Test
    @DisplayName("Получение списка всех объявлений")
    void getAllAds_shouldReturnAllAds_whenAdsExist() {
        // Получаем все объявления через сервис
        Ads result = adService.getAllAds();

        // Проверяем структуру результата
        assertNotNull(result, "Результат не должен быть null");
        assertEquals(3, result.getCount(), "Должно быть 3 объявления в БД");
        assertEquals(3, result.getResults().size(), "Список должен содержать 3 элемента");

        // Проверяем каждое объявление в списке
        result.getResults().forEach(ad -> {
            assertNotNull(ad.getPk(), "ID объявления не должен быть null");
            assertNotNull(ad.getAuthor(), "Автор не должен быть null");
            assertNotNull(ad.getTitle(), "Заголовок не должен быть null");
            assertNotNull(ad.getPrice(), "Цена не должна быть null");
            assertTrue(ad.getPrice() >= 0, "Цена не должна быть отрицательной");
            assertNull(ad.getImage(), "Изображение должно быть null (не загружено)");
        });

        // Проверяем, что все созданные объявления присутствуют
        List<String> expectedTitles = List.of("Велосипед горный", "Ноутбук игровой", "Книги по программированию");
        List<String> actualTitles = result.getResults().stream()
                .map(Ad::getTitle)
                .toList();

        assertThat(actualTitles).containsExactlyInAnyOrderElementsOf(expectedTitles);
    }

    /**
     * Тестирует получение объявлений текущего пользователя (фильтрация по автору).
     * <p>
     * <b>Сценарий:</b> USER запрашивает список только своих объявлений.
     * <p>
     * <b>Проверяемые аспекты:</b>
     * <ul>
     *   <li>Фильтрация объявлений по автору (только свои объявления)</li>
     *   <li>Исключение объявлений других пользователей из результата</li>
     *   <li>Корректность определения автора через аутентификацию</li>
     * </ul>
     */
    @Test
    @DisplayName("Получение объявлений текущего пользователя (фильтрация по автору)")
    void getUserAds_shouldReturnOnlyUserAds_whenUserHasAds() {

        // Получаем объявления только для user1
        Ads result = adService.getUserAds(user1Auth);

        // Проверяем результат
        assertEquals(2, result.getCount(), "User1 должен иметь 2 объявления");
        assertEquals(2, result.getResults().size(), "Список должен содержать 2 элемента");

        // Проверяем, что все объявления принадлежат user1
        result.getResults().forEach(ad -> assertEquals(user1.getId(), ad.getAuthor(),
                "Все объявления должны принадлежать user1"));

        // Проверяем заголовки объявлений user1
        List<String> expectedTitles = List.of("Велосипед горный", "Ноутбук игровой");
        List<String> actualTitles = result.getResults().stream()
                .map(Ad::getTitle)
                .toList();

        assertThat(actualTitles).containsExactlyInAnyOrderElementsOf(expectedTitles);
    }

    /**
     * Тестирует получение полной информации об объявлении.
     * <p>
     * <b>Сценарий:</b> Запрос расширенной информации об существующем объявлении.
     * <p>
     * <b>Проверяемые аспекты:</b>
     * <ul>
     *   <li>Корректность преобразования в ExtendedAd DTO</li>
     *   <li>Наличие всех полей: описание, контактные данные автора</li>
     *   <li>Совпадение данных с сущностью в БД</li>
     *   <li>Null-безопасность для поля image</li>
     * </ul>
     */
    @Test
    @DisplayName("Получение полной информации об объявлении")
    void getAd_shouldReturnExtendedAd_whenAdExists() {
        // Получаем сохранённое объявление из репозитория
        AdEntity expectedAd = adRepository.findById(savedAdId)
                .orElseThrow(() -> new AssertionError("Тестовое объявление должно существовать"));

        //  Получаем расширенную информацию об объявлении через сервис
        ExtendedAd result = adService.getAd(savedAdId);

        // Проверяем все поля ExtendedAd DTO
        assertNotNull(result, "Результат не должен быть null");
        assertEquals(expectedAd.getId(), result.getPk(), "ID должен совпадать");
        assertEquals(expectedAd.getTitle(), result.getTitle(), "Заголовок должен совпадать");
        assertEquals(expectedAd.getPrice(), result.getPrice(), "Цена должна совпадать");
        assertEquals(expectedAd.getDescription(), result.getDescription(), "Описание должно совпадать");

        // Проверяем данные автора (из связанного UserEntity)
        assertEquals(expectedAd.getAuthor().getFirstName(), result.getAuthorFirstName(),
                "Имя автора должно совпадать");
        assertEquals(expectedAd.getAuthor().getLastName(), result.getAuthorLastName(),
                "Фамилия автора должна совпадать");
        assertEquals(expectedAd.getAuthor().getEmail(), result.getEmail(),
                "Email автора должен совпадать");
        assertEquals(expectedAd.getAuthor().getPhone(), result.getPhone(),
                "Телефон автора должен совпадать");

        // Изображение должно быть null (так как не загружено в тестах)
        assertNull(result.getImage(), "Изображение должно быть null (не загружено в тестах)");
    }

    /**
     * Тестирует обработку запроса несуществующего объявления.
     * <p>
     * <b>Сценарий:</b> Запрос информации об объявлении с несуществующим ID.
     * <p>
     * <b>Проверяемые аспекты:</b>
     * <ul>
     *   <li>Выбрасывание {@link AdNotFoundException}</li>
     *   <li>Корректное сообщение об ошибке с указанием ID</li>
     *   <li>Автоматическая конвертация в HTTP 404 через {@link GlobalExceptionHandler}</li>
     * </ul>
     */
    @Test
    @DisplayName("Исключение при запросе несуществующего объявления")
    void getAd_shouldThrowAdNotFoundException_whenAdNotExists() {
        // Генерируем заведомо несуществующий ID
        Integer nonExistentId = 9999;

        // Проверяем, что исключение выбрасывается
        AdNotFoundException exception = assertThrows(AdNotFoundException.class, () ->
                adService.getAd(nonExistentId),
                "Должно быть выброшено AdNotFoundException для несуществующего ID");

        //  Проверяем сообщение исключения содержит ID
        assertThat(exception.getMessage()).contains(nonExistentId.toString());
    }

    /**
     * Тестирует получение пустого списка объявлений для пользователя без объявлений.
     * <p>
     * <b>Сценарий:</b> Пользователь без объявлений запрашивает свои объявления.
     * <p>
     * <b>Проверяемые аспекты:</b>
     * <ul>
     *   <li>Возврат корректной структуры Ads с count=0</li>
     *   <li>Пустой список results (не null)</li>
     *   <li>Обработка edge-case: пользователь без объявлений</li>
     *   <li>Корректная работа с аутентификацией для такого пользователя</li>
     * </ul>
     */
    @Test
    @DisplayName("Получение пустого списка объявлений для пользователя без объявлений")
    void getUserAds_shouldReturnEmptyList_whenUserHasNoAds() {
        // Создаём нового пользователя без объявлений
        UserEntity userWithoutAds = createTestUser("no.ads@example.com", "Нет", "Объявлений", Role.USER);
        Authentication authentication = TestAuthenticationUtils.createAuthentication(userWithoutAds);

        // Получаем объявления пользователя без объявлений
        Ads result = adService.getUserAds(authentication);

        // Проверяем структуру пустого результата
        assertNotNull(result, "Результат не должен быть null");
        assertEquals(0, result.getCount(), "Count должен быть 0 для пользователя без объявлений");
        assertNotNull(result.getResults(), "Список results не должен быть null");
        assertTrue(result.getResults().isEmpty(), "Список results должен быть пустым");
    }

    /**
     * Тестирует обработку AccessDeniedException при попытке удалить чужое объявление.
     * <p>
     * <b>Сценарий:</b> USER пытается удалить объявление другого пользователя (не ADMIN).
     * <p>
     * <b>Проверяемые аспекты:</b>
     * <ul>
     *   <li>Выбрасывание {@link AccessDeniedException}</li>
     *   <li>Сохранение объявления в БД (не удалено)</li>
     *   <li>Корректность проверки прав доступа в сервисе</li>
     * </ul>
     */
    @Test
    @DisplayName("Исключение при попытке удалить чужое объявление")
    void deleteAd_shouldThrowAccessDeniedException_whenUserTriesToDeleteOthersAd() {

        Integer adIdToDelete = adRepository.findAllByAuthorId(user1.getId()).get(0).getId();
        int initialAdCount = adRepository.findAll().size();

        //  Проверяем, что исключение выбрасывается
        assertThrows(AccessDeniedException.class, () ->
                adService.deleteAd(adIdToDelete, user2Auth),
                "Должно быть выброшено AccessDeniedException при попытке удалить чужое объявление");

        // Проверяем, что объявление не удалено из БД
        int finalAdCount = adRepository.findAll().size();
        assertEquals(initialAdCount, finalAdCount, "Количество объявлений не должно измениться");
        assertTrue(adRepository.existsById(adIdToDelete), "Объявление должно остаться в БД");
    }

    /**
     * Тестирует успешное удаление своего объявления.
     * <p>
     * <b>Сценарий:</b> USER удаляет свое собственное объявление.
     * <p>
     * <b>Проверяемые аспекты:</b>
     * <ul>
     *   <li>Успешное удаление без исключений</li>
     *   <li>Уменьшение количества объявлений в БД</li>
     *   <li>Отсутствие объявления в БД после удаления</li>
     * </ul>
     */
    @Test
    @DisplayName("Успешное удаление своего объявления")
    void deleteAd_shouldDeleteAd_whenUserIsAuthor() {

        Integer adIdToDelete = adRepository.findAllByAuthorId(user1.getId()).get(0).getId();
        int initialAdCount = adRepository.findAll().size();

        // Удаляем объявление
        adService.deleteAd(adIdToDelete, user1Auth);

        // Проверяем, что объявление удалено
        int finalAdCount = adRepository.findAll().size();
        assertEquals(initialAdCount - 1, finalAdCount, "Количество объявлений должно уменьшиться на 1");
        assertFalse(adRepository.existsById(adIdToDelete), "Объявление не должно существовать в БД");
    }

    /**
     * Тестирует успешное удаление любого объявления администратором.
     * <p>
     * <b>Сценарий:</b> ADMIN удаляет объявление другого пользователя.
     * <p>
     * <b>Проверяемые аспекты:</b>
     * <ul>
     *   <li>Успешное удаление без исключений</li>
     *   <li>Административные привилегии работают корректно</li>
     *   <li>Уменьшение количества объявлений в БД</li>
     * </ul>
     */
    @Test
    @DisplayName("Успешное удаление любого объявления администратором")
    void deleteAd_shouldDeleteAnyAd_whenUserIsAdmin() {

        Integer adIdToDelete = adRepository.findAllByAuthorId(user1.getId()).get(0).getId();
        int initialAdCount = adRepository.findAll().size();

        //  Администратор удаляет объявление
        adService.deleteAd(adIdToDelete, adminUserAuth);

        //  Проверяем, что объявление удалено
        int finalAdCount = adRepository.findAll().size();
        assertEquals(initialAdCount - 1, finalAdCount, "Количество объявлений должно уменьшиться на 1");
        assertFalse(adRepository.existsById(adIdToDelete), "Объявление не должно существовать в БД");
    }

    /**
     * Тестирует успешное обновление своего объявления.
     * <p>
     * <b>Сценарий:</b> USER обновляет заголовок и цену своего объявления.
     * <p>
     * <b>Проверяемые аспекты:</b>
     * <ul>
     *   <li>Корректное обновление полей в БД</li>
     *   <li>Возврат обновленного DTO</li>
     *   <li>Сохранение неизмененных полей</li>
     * </ul>
     */
    @Test
    @DisplayName("Успешное обновление своего объявления")
    void updateAd_shouldUpdateAd_whenUserIsAuthor() {

        Integer adIdToUpdate = adRepository.findAllByAuthorId(user1.getId()).get(0).getId();

        CreateOrUpdateAd updateData = new CreateOrUpdateAd();
        updateData.setTitle("Обновленный заголовок");
        updateData.setPrice(20000);
        updateData.setDescription("Обновленное описание");

        // Обновляем объявление
        Ad updatedAd = adService.updateAd(adIdToUpdate, user1Auth, updateData);

        // Проверяем возвращаемый DTO
        assertEquals(adIdToUpdate, updatedAd.getPk(), "ID должен совпадать");
        assertEquals("Обновленный заголовок", updatedAd.getTitle(), "Заголовок должен быть обновлен");
        assertEquals(20000, updatedAd.getPrice(), "Цена должна быть обновлена");

        // Проверяем фактическое состояние в БД
        AdEntity updatedEntity = adRepository.findById(adIdToUpdate)
                .orElseThrow(() -> new AssertionError("Объявление должно существовать в БД"));
        assertEquals("Обновленный заголовок", updatedEntity.getTitle(), "Заголовок в БД должен быть обновлен");
        assertEquals(20000, updatedEntity.getPrice(), "Цена в БД должна быть обновлена");
        assertEquals("Обновленное описание", updatedEntity.getDescription(), "Описание в БД должно быть обновлено");
    }

    /**
     * Вспомогательный метод для создания тестового пользователя.
     * <p>
     * Создает пользователя с указанными данными, хеширует пароль
     * и сохраняет в БД. Используется для подготовки тестовых данных.
     *
     * @param email    email пользователя (используется как логин)
     * @param firstName имя пользователя
     * @param lastName  фамилия пользователя
     * @param role      роль пользователя (USER или ADMIN)
     * @return сохраненная сущность пользователя
     */
    private UserEntity createTestUser(String email, String firstName, String lastName, Role role) {
        UserEntity user = new UserEntity(
                email,
                passwordEncoder.encode("password123"),  // Хешируем пароль
                firstName,
                lastName,
                "+7 (999) 000-00-00",
                role
        );
        user.setEnabled(true);
        return userRepository.save(user);
    }

}
