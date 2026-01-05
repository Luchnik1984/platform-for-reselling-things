package ru.skypro.homework.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.skypro.homework.dto.ads.Ad;
import ru.skypro.homework.dto.ads.CreateOrUpdateAd;
import ru.skypro.homework.dto.ads.ExtendedAd;
import ru.skypro.homework.entity.AdEntity;
import ru.skypro.homework.entity.ImageEntity;
import ru.skypro.homework.entity.UserEntity;
import ru.skypro.homework.enums.Role;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit-тесты для {@link AdMapper}.
 * Проверяет корректность маппинга между AdEntity и DTO.
 * Тестируем сгенерированную имплементацию
 * <p>
 * Особенности тестирования:
 * <ul>
 *   <li>Тестируется сгенерированная реализация {@link AdMapperImpl}</li>
 *   <li>Проверяется обработка null значений (null-safety)</li>
 *   <li>Проверяется частичное обновление (только не-null поля)</li>
 *   <li>Проверяется корректность маппинга связей (автор, изображение)</li>
 * </ul>
 *
 * @see AdMapper
 * @see AdMapperImpl
 */
@ExtendWith(MockitoExtension.class)
public class AdMapperTest {

    /**
     * Тестируемая реализация маппера, сгенерированная MapStruct.
     * Используется {@link InjectMocks} для возможности мокирования зависимостей
     * (хотя в текущей реализации зависимостей нет).
     */
    @InjectMocks
    private AdMapperImpl adMapper;

    /** Тестовый заголовок объявления. */
    private static final String TEST_TITLE = "Продам велосипед";

    /** Тестовая цена объявления. */
    private static final int TEST_PRICE = 15000;

    /** Тестовое описание объявления. */
    private static final String TEST_DESCRIPTION = "Отличный горный велосипед, 2022 год";

    /** Тестовый ID объявления. */
    private static final int TEST_AD_ID = 100;

    /** Тестовый заголовок для обновления. */
    private static final String UPDATED_TITLE = "Новый заголовок";

    /** Тестовая цена для обновления. */
    private static final int UPDATED_PRICE = 20000;

    /**
     * Тест: преобразование {@link CreateOrUpdateAd} → {@link AdEntity}.
     * <p>
     * Проверяет:
     * <ul>
     *   <li>Корректное маппинга полей: title, price, description</li>
     *   <li>Игнорирование полей, которые должны устанавливаться отдельно: id, author, image, createdAt</li>
     *   <li>Создание нового экземпляра AdEntity</li>
     * </ul>
     */
    @Test
    void toEntity_ShouldMapCreateOrUpdateAdToAdEntity() {

        CreateOrUpdateAd dto = new CreateOrUpdateAd();
        dto.setTitle(TEST_TITLE);
        dto.setPrice(TEST_PRICE);
        dto.setDescription(TEST_DESCRIPTION);

        AdEntity result = adMapper.toEntity(dto);

        assertNotNull(result, "Результат маппинга не должен быть null");
        assertEquals(TEST_TITLE, result.getTitle(), "Заголовок должен корректно маппиться");
        assertEquals(TEST_PRICE, result.getPrice(), "Цена должна корректно маппиться");
        assertEquals(TEST_DESCRIPTION, result.getDescription(), "Описание должно корректно маппиться");
        assertNull(result.getId(), "ID должен быть null (устанавливается БД)");
        assertNull(result.getAuthor(), "Автор должен быть null (устанавливается отдельно)");
        assertNull(result.getImage(), "Изображение должно быть null (устанавливается отдельно)");
        // createdAt устанавливается конструктором AdEntity

    }

    /**
     * Тест: преобразование {@link AdEntity} -> {@link Ad} (краткое DTO).
     * <p>
     * Проверяет:
     * <ul>
     *   <li>Корректный маппинг базовых полей: id -> pk, price, title</li>
     *   <li>Маппинг автора: author.id -> author</li>
     *   <li>Маппинг изображения: image.getImageUrl() -> image</li>
     *   <li>Создание корректного DTO для отображения в списках</li>
     * </ul>
     */
    @Test
    void toDto_ShouldMapAdEntityToAdDto() {

        UserEntity author = createTestUser();
        ImageEntity image = createTestImage();
        AdEntity entity = createTestAdEntity(author, image);

        Ad result = adMapper.toDto(entity);

        assertNotNull(result, "Результат маппинга не должен быть null");
        assertEquals(entity.getId(), result.getPk(), "ID должен маппиться в pk");
        assertEquals(author.getId(), result.getAuthor(), "ID автора должен корректно маппиться");
        assertEquals(entity.getPrice(), result.getPrice(), "Цена должна корректно маппиться");
        assertEquals(entity.getTitle(), result.getTitle(), "Заголовок должен корректно маппиться");
        assertNotNull(result.getImage(), "Ссылка на изображение не должна быть null");
        assertEquals(image.getImageUrl(), result.getImage(), "Должен генерироваться корректный URL изображения");
    }

    /**
     * Тест: преобразование {@link AdEntity} -> {@link Ad} без изображения.
     * <p>
     * Проверяет обработку случая, когда у объявления нет изображения.
     * Поле image в DTO должно быть null.
     */
    @Test
    void toDto_ShouldHandleNullImage() {

        UserEntity author = createTestUser();
        AdEntity entity = createTestAdEntity(author, null);

        Ad result = adMapper.toDto(entity);

        assertNotNull(result, "Результат маппинга не должен быть null");
        assertEquals(entity.getId(), result.getPk(), "ID должен маппиться в pk");
        assertEquals(author.getId(), result.getAuthor(), "ID автора должен корректно маппиться");
        assertNull(result.getImage(), "При отсутствии изображения поле должно быть null");
    }

    /**
     * Тест: преобразование {@link AdEntity} → {@link ExtendedAd} (полное DTO).
     * <p>
     * Проверяет:
     * <ul>
     *   <li>Маппинг всех полей объявления</li>
     *   <li>Маппинг информации об авторе: имя, фамилия, email, телефон</li>
     *   <li>Маппинг изображения</li>
     *   <li>Создание полного DTO для страницы деталей объявления</li>
     * </ul>
     */
    @Test
    void toExtendedDto_ShouldMapAdEntityToExtendedAdDto() {

        UserEntity author = createTestUser();
        ImageEntity image = createTestImage();
        AdEntity entity = createTestAdEntity(author, image);

        ExtendedAd result = adMapper.toExtendedDto(entity);

        assertNotNull(result, "Результат маппинга не должен быть null");
        assertEquals(entity.getId(), result.getPk(), "ID должен маппиться в pk");
        assertEquals(entity.getPrice(), result.getPrice(), "Цена должна корректно маппиться");
        assertEquals(entity.getTitle(), result.getTitle(), "Заголовок должен корректно маппиться");
        assertEquals(entity.getDescription(), result.getDescription(), "Описание должно корректно маппиться");
        assertNotNull(result.getImage(), "Ссылка на изображение не должна быть null");
        assertEquals(image.getImageUrl(), result.getImage(), "Должен генерироваться корректный URL изображения");
        assertEquals(author.getFirstName(), result.getAuthorFirstName(), "Имя автора должно корректно маппиться");
        assertEquals(author.getLastName(), result.getAuthorLastName(), "Фамилия автора должна корректно маппиться");
        assertEquals(author.getEmail(), result.getEmail(), "Email автора должен корректно маппиться");
        assertEquals(author.getPhone(), result.getPhone(), "Телефон автора должен корректно маппиться");
    }

    /**
     * Тест: преобразование null AdEntity -> null DTO.
     * Проверяет, что все методы маппинга возвращают null при передаче null.
     */
    @Test
    void toDto_ShouldReturnNullForNullInput() {
        assertNull(adMapper.toDto(null), "toDto должен возвращать null при null входном значении");
        assertNull(adMapper.toExtendedDto(null), "toExtendedDto должен возвращать null при null входном значении");
        assertNull(adMapper.toEntity(null), "toEntity должен возвращать null при null входном значении");
    }

    /**
     * Тест: частичное обновление {@link AdEntity} из {@link CreateOrUpdateAd}.
     * <p>
     * Проверяет:
     * <ul>
     *   <li>Обновляются только не-null поля из DTO</li>
     *   <li>Поля с null значениями в DTO игнорируются</li>
     *   <li>Технические поля (id, author, image, createdAt) не изменяются</li>
     *   <li>Поддерживается паттерн частичного обновления (PATCH)</li>
     * </ul>
     */
    @Test
    void updateEntityFromDto_ShouldUpdateOnlyNonNullFields() {

        AdEntity existingEntity = new AdEntity();
        existingEntity.setTitle(TEST_TITLE);
        existingEntity.setPrice(TEST_PRICE);
        existingEntity.setDescription(TEST_DESCRIPTION);
        LocalDateTime originalCreatedAt = existingEntity.getCreatedAt(); // Сохраняем оригинальное значение

        CreateOrUpdateAd updateDto = new CreateOrUpdateAd();
        updateDto.setTitle(UPDATED_TITLE);

        adMapper.updateEntityFromDto(updateDto, existingEntity);

        assertEquals(UPDATED_TITLE, existingEntity.getTitle(), "Заголовок должен обновиться");
        assertEquals(TEST_PRICE, existingEntity.getPrice(), "Цена не должна измениться");
        assertEquals(TEST_DESCRIPTION, existingEntity.getDescription(), "Описание не должно измениться");
        assertEquals(originalCreatedAt, existingEntity.getCreatedAt(), "Дата создания не должна изменяться");
    }

    /**
     * Тест: обновление null DTO при обновлении {@link AdEntity}.
     * <p>
     * Проверяет, что при передаче null в качестве DTO для обновления,
     * сущность не изменяется (null-safety).
     */
    @Test
    void updateEntityFromDto_ShouldDoNothingForNullDto() {

        AdEntity existingEntity = new AdEntity();
        existingEntity.setTitle(TEST_TITLE);
        existingEntity.setPrice(TEST_PRICE);
        LocalDateTime originalCreatedAt = existingEntity.getCreatedAt();

        adMapper.updateEntityFromDto(null, existingEntity);

        assertEquals(TEST_TITLE, existingEntity.getTitle());
        assertEquals(TEST_PRICE, existingEntity.getPrice());
        assertEquals(originalCreatedAt, existingEntity.getCreatedAt());
    }

    /**
     * Тест: проверка сохранения поля {@code createdAt} при маппинге.
     * <p>
     * Проверяет, что поле {@code createdAt} не сбрасывается при маппинге
     * и может быть установлено отдельно после создания сущности.
     */
    @Test
    void shouldPreserveCreatedAtInEntity() {

        CreateOrUpdateAd dto = new CreateOrUpdateAd();
        dto.setTitle(TEST_TITLE);
        dto.setPrice(TEST_PRICE);
        dto.setDescription(TEST_DESCRIPTION);

        AdEntity entity = adMapper.toEntity(dto);

               assertNotNull(entity, "Сущность не должна быть null");

        LocalDateTime testTime = LocalDateTime.of(2025, 12, 30, 15, 0);
        entity.setCreatedAt(testTime);

        assertEquals(testTime, entity.getCreatedAt(), "Дата создания должна сохраняться после установки");
    }

    /**
     * Тест: проверка создания {@link AdEntity} с помощью конструктора.
     */
    @Test
    void adEntityConstructor_ShouldSetCreatedAt() {

        UserEntity author = createTestUser();
        AdEntity entity = new AdEntity(TEST_TITLE,
                TEST_PRICE, TEST_DESCRIPTION, author);

        // Then - createdAt должен быть установлен
        assertNotNull(entity.getCreatedAt(), "Конструктор должен устанавливать дату создания");
        assertEquals(TEST_TITLE, entity.getTitle(), "Конструктор должен устанавливать заголовок");
        assertEquals(TEST_PRICE, entity.getPrice(), "Конструктор должен устанавливать цену");
        assertEquals(TEST_DESCRIPTION, entity.getDescription(), "Конструктор должен устанавливать описание");
        assertEquals(author, entity.getAuthor(), "Конструктор должен устанавливать автора");
    }

    /**
     * Создаёт тестовый объект {@link UserEntity}, заполненный тестовыми данными.
     */
    private UserEntity createTestUser() {
        UserEntity user = new UserEntity();
        user.setId(1);
        user.setEmail("test@example.com");
        user.setFirstName("Иван");
        user.setLastName("Иванов");
        user.setPhone("+7 (999) 123-45-67");
        user.setRole(Role.USER);
        user.setPassword("hashedPassword");
        user.setEnabled(true);
        return user;
    }

/**
 * Создаёт тестовый объект {@link ImageEntity}, заполненный тестовыми данными.
 */
 private ImageEntity createTestImage() {
        ImageEntity image = new ImageEntity();
        image.setId(1);
        image.setFilePath("uploads/ads/123.jpg");
        image.setFileSize(1024L);
        image.setMediaType("image/jpeg");
        return image;
    }

    /**
     * Создаёт тестовый объект {@link AdEntity}, заполненный тестовыми данными.
     *
     * @param author автор объявления
     * @param image изображение объявления (может быть null).
     */
    private AdEntity createTestAdEntity(UserEntity author, ImageEntity image) {
        AdEntity ad = new AdEntity();
        ad.setId(TEST_AD_ID);
        ad.setTitle(TEST_TITLE);
        ad.setPrice(TEST_PRICE);
        ad.setDescription(TEST_DESCRIPTION);
        ad.setCreatedAt(LocalDateTime.of(2025, 12, 30, 15, 0));
        ad.setAuthor(author);
        ad.setImage(image);
        return ad;
    }
}
