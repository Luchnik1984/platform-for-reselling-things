package ru.skypro.homework.mapper;

import org.mapstruct.*;
import ru.skypro.homework.dto.ads.Ad;
import ru.skypro.homework.dto.ads.CreateOrUpdateAd;
import ru.skypro.homework.dto.ads.ExtendedAd;
import ru.skypro.homework.entity.AdEntity;

/**
 * Маппер для преобразования между сущностью объявления (Ad) и DTO.
 *
 * <p>Преобразования:
 * <ul>
 *   <li>{@link CreateOrUpdateAd} → {@link AdEntity} (при создании/обновлении)</li>
 *   <li>{@link AdEntity} → {@link Ad} (краткая информация для списков)</li>
 *   <li>{@link AdEntity} → {@link ExtendedAd} (полная информация)</li>
 * </ul>
 *
 * @see AdEntity
 * @see CreateOrUpdateAd
 * @see Ad
 * @see ExtendedAd
 */
@Mapper(config = MapStructConfig.class)
public interface AdMapper {

    /**
     * Преобразует DTO создания/обновления в сущность объявления (Ad).
     *
     * @param dto DTO с данными объявления
     * @return сущность объявления
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "image", ignore = true)
    @Mapping(target = "comments", ignore = true)
    AdEntity toEntity(CreateOrUpdateAd dto);

    /**
     * Преобразует сущность объявления (Ad) в краткое DTO (для списков).
     *
     * <p>Включает:
     * <ul>
     *   <li>ID объявления ({@code pk})</li>
     *   <li>ID автора ({@code author})</li>
     *   <li>Ссылку на изображение (если есть)</li>
     *   <li>Цену и заголовок</li>
     * </ul>
     *
     * @param adEntity сущность объявления
     * @return краткое DTO объявления
     */
    @Mapping(target = "pk", source = "id")
    @Mapping(target = "author", source = "author.id")
    @Mapping(target = "image", expression = "java(adEntity.getImage()" +
            " != null ? adEntity.getImage().getImageUrl() : null)")
    Ad toDto(AdEntity adEntity);

    /**
     * Преобразует сущность объявления (Ad) в полное DTO.
     *
     * <p>Включает все поля из {@link #toDto(AdEntity)} плюс:
     * <ul>
     *   <li>Имя и фамилию автора</li>
     *   <li>Email автора</li>
     *   <li>Телефон автора</li>
     *   <li>Описание объявления</li>
     * </ul>
     *
     * @param adEntity сущность объявления
     * @return полное DTO объявления
     */
    @Mapping(target = "pk", source = "id")
    @Mapping(target = "authorFirstName", source = "author.firstName")
    @Mapping(target = "authorLastName", source = "author.lastName")
    @Mapping(target = "email", source = "author.email")
    @Mapping(target = "phone", source = "author.phone")
    @Mapping(target = "image", expression = "java(adEntity.getImage()" +
            " != null ? adEntity.getImage().getImageUrl() : null)")
    ExtendedAd toExtendedDto(AdEntity adEntity);

    /**
     * Обновляет сущность объявления (Ad) из DTO.
     * Используется для частичного обновления объявления.
     *
     * @param dto      DTO с обновлениями
     * @param adEntity сущность для обновления
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "image", ignore = true)
    @Mapping(target = "comments", ignore = true)
    void updateEntityFromDto(CreateOrUpdateAd dto, @MappingTarget AdEntity adEntity);

}

