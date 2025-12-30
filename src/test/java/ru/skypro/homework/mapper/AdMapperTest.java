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
 */
@ExtendWith(MockitoExtension.class)
public class AdMapperTest {

    @InjectMocks
    private AdMapperImpl adMapper;

    /**
     * Тест: преобразование CreateOrUpdateAd → AdEntity.
     */
    @Test
    void toEntity_ShouldMapCreateOrUpdateAdToAdEntity() {

        CreateOrUpdateAd dto = new CreateOrUpdateAd();
        dto.setTitle("Продам велосипед");
        dto.setPrice(15000);
        dto.setDescription("Отличный горный велосипед, 2022 год");


        AdEntity result = adMapper.toEntity(dto);

        assertNotNull(result);
        assertEquals("Продам велосипед", result.getTitle());
        assertEquals(15000, result.getPrice());
        assertEquals("Отличный горный велосипед, 2022 год", result.getDescription());
        assertNull(result.getId());
        assertNull(result.getAuthor());
        assertNull(result.getImage());
        // createdAt устанавливается конструктором AdEntity

    }

    /**
     * Тест: преобразование AdEntity → Ad (краткое DTO).
     */
    @Test
    void toDto_ShouldMapAdEntityToAdDto() {

        UserEntity author = createTestUser();
        ImageEntity image = createTestImage();
        AdEntity entity = createTestAdEntity(author, image);

        Ad result = adMapper.toDto(entity);

        assertNotNull(result);
        assertEquals(entity.getId(), result.getPk());
        assertEquals(author.getId(), result.getAuthor());
        assertEquals(entity.getPrice(), result.getPrice());
        assertEquals(entity.getTitle(), result.getTitle());
        assertNotNull(result.getImage());
        assertEquals(image.getImageUrl(), result.getImage());
    }

    /**
     * Тест: преобразование AdEntity → Ad без изображения.
     */
    @Test
    void toDto_ShouldHandleNullImage() {

        UserEntity author = createTestUser();
        AdEntity entity = createTestAdEntity(author, null);

        Ad result = adMapper.toDto(entity);

        assertNotNull(result);
        assertEquals(entity.getId(), result.getPk());
        assertEquals(author.getId(), result.getAuthor());
        assertNull(result.getImage());
    }

    /**
     * Тест: преобразование AdEntity → ExtendedAd.
     */
    @Test
    void toExtendedDto_ShouldMapAdEntityToExtendedAdDto() {

        UserEntity author = createTestUser();
        ImageEntity image = createTestImage();
        AdEntity entity = createTestAdEntity(author, image);

        ExtendedAd result = adMapper.toExtendedDto(entity);

        assertNotNull(result);
        assertEquals(entity.getId(), result.getPk());
        assertEquals(entity.getPrice(), result.getPrice());
        assertEquals(entity.getTitle(), result.getTitle());
        assertEquals(entity.getDescription(), result.getDescription());
        assertNotNull(result.getImage());
        assertEquals(image.getImageUrl(), result.getImage());
        assertEquals(author.getFirstName(), result.getAuthorFirstName());
        assertEquals(author.getLastName(), result.getAuthorLastName());
        assertEquals(author.getEmail(), result.getEmail());
        assertEquals(author.getPhone(), result.getPhone());
    }

    /**
     * Тест: преобразование null AdEntity → null DTO.
     */
    @Test
    void toDto_ShouldReturnNullForNullInput() {
        assertNull(adMapper.toDto(null));
        assertNull(adMapper.toExtendedDto(null));
        assertNull(adMapper.toEntity(null));
    }

    /**
     * Тест: частичное обновление AdEntity.
     */
    @Test
    void updateEntityFromDto_ShouldUpdateOnlyNonNullFields() {

        AdEntity existingEntity = new AdEntity();
        existingEntity.setTitle("Старый заголовок");
        existingEntity.setPrice(1000);
        existingEntity.setDescription("Старое описание");
        LocalDateTime originalCreatedAt = existingEntity.getCreatedAt(); // Сохраняем оригинальное значение

        CreateOrUpdateAd updateDto = new CreateOrUpdateAd();
        updateDto.setTitle("Новый заголовок");

        adMapper.updateEntityFromDto(updateDto, existingEntity);

        assertEquals("Новый заголовок", existingEntity.getTitle());
        assertEquals(1000, existingEntity.getPrice());
        assertEquals("Старое описание", existingEntity.getDescription());
        assertEquals(originalCreatedAt, existingEntity.getCreatedAt()); // createdAt не изменился
    }

    /**
     * Тест: обновление null DTO.
     */
    @Test
    void updateEntityFromDto_ShouldDoNothingForNullDto() {

        AdEntity existingEntity = new AdEntity();
        existingEntity.setTitle("Оригинальный заголовок");
        existingEntity.setPrice(5000);
        LocalDateTime originalCreatedAt = existingEntity.getCreatedAt();

        adMapper.updateEntityFromDto(null, existingEntity);

        assertEquals("Оригинальный заголовок", existingEntity.getTitle());
        assertEquals(5000, existingEntity.getPrice());
        assertEquals(originalCreatedAt, existingEntity.getCreatedAt());
    }

    /**
     * Тест: проверка маппинга createdAt.
     * Внимание: AdEntity устанавливает createdAt по умолчанию в конструкторе!
     */
    @Test
    void shouldPreserveCreatedAtInEntity() {
        // Given
        CreateOrUpdateAd dto = new CreateOrUpdateAd();
        dto.setTitle("Тест");
        dto.setPrice(1000);
        dto.setDescription("Тестовое описание");

        AdEntity entity = adMapper.toEntity(dto);

               assertNotNull(entity);

        LocalDateTime testTime = LocalDateTime.of(2025, 12, 30, 15, 0);
        entity.setCreatedAt(testTime);

        assertEquals(testTime, entity.getCreatedAt());
    }

    /**
     * Тест: проверка создания AdEntity с помощью конструктора.
     */
    @Test
    void adEntityConstructor_ShouldSetCreatedAt() {

        UserEntity author = createTestUser();
        AdEntity entity = new AdEntity("Тест", 1000, "Описание", author);

        // Then - createdAt должен быть установлен
        assertNotNull(entity.getCreatedAt());
        assertEquals("Тест", entity.getTitle());
        assertEquals(1000, entity.getPrice());
        assertEquals("Описание", entity.getDescription());
        assertEquals(author, entity.getAuthor());
    }


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

    private ImageEntity createTestImage() {
        ImageEntity image = new ImageEntity();
        image.setId(1);
        image.setFilePath("uploads/ads/123.jpg");
        image.setFileSize(1024L);
        image.setMediaType("image/jpeg");
        return image;
    }

    private AdEntity createTestAdEntity(UserEntity author, ImageEntity image) {
        AdEntity ad = new AdEntity();
        ad.setId(100);
        ad.setTitle("Продам велосипед");
        ad.setPrice(15000);
        ad.setDescription("Отличный горный велосипед");
        ad.setCreatedAt(LocalDateTime.of(2025, 12, 30, 15, 0));
        ad.setAuthor(author);
        ad.setImage(image);
        return ad;
    }
}
