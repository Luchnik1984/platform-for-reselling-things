package ru.skypro.homework.mapper;

import org.springframework.stereotype.Component;
import ru.skypro.homework.dto.ads.Ad;
import ru.skypro.homework.dto.ads.CreateOrUpdateAd;
import ru.skypro.homework.dto.ads.ExtendedAd;
import ru.skypro.homework.entity.AdEntity;
import ru.skypro.homework.entity.UserEntity;

/**
 * Реализация маппера для преобразования между сущностью объявления и DTO.
 *
 * <p>Аннотация {@link Component} делает этот класс Spring bean,
 * что позволяет использовать dependency injection.
 *
 * @see AdMapper
 */
@Component
public class AdMapperImpl implements AdMapper {
    /**
     * {@inheritDoc}
     */
    @Override
    public AdEntity toEntity(CreateOrUpdateAd dto) {
        if (dto == null) {
            return null;
        }

        AdEntity entity = new AdEntity();
        entity.setTitle(dto.getTitle());
        entity.setPrice(dto.getPrice());
        entity.setDescription(dto.getDescription());

        // Поля createdAt, author, image, comments
        // должны устанавливаться отдельно в сервисе

        return entity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Ad toDto(AdEntity entity) {
        if (entity == null) {
            return null;
        }

        Ad dto = new Ad();
        dto.setPk(entity.getId());

        if (entity.getAuthor() != null) {
            dto.setAuthor(entity.getAuthor().getId());
        }

        dto.setTitle(entity.getTitle());
        dto.setPrice(entity.getPrice());

        if (entity.getImage() != null) {
            dto.setImage(entity.getImage().getImageUrl());
        }

        return dto;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExtendedAd toExtendedDto(AdEntity entity) {
        if (entity == null) {
            return null;
        }

        ExtendedAd dto = new ExtendedAd();
        dto.setPk(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setPrice(entity.getPrice());
        dto.setDescription(entity.getDescription());

        if (entity.getAuthor() != null) {
            UserEntity author = entity.getAuthor();
            dto.setAuthorFirstName(author.getFirstName());
            dto.setAuthorLastName(author.getLastName());
            dto.setEmail(author.getEmail());
            dto.setPhone(author.getPhone());
        }

        if (entity.getImage() != null) {
            dto.setImage(entity.getImage().getImageUrl());
        }

        return dto;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateEntityFromDto(CreateOrUpdateAd dto, AdEntity entity) {
        if (dto == null || entity == null) {
            return;
        }

        // Обновляем только не-null поля
        if (dto.getTitle() != null) {
            entity.setTitle(dto.getTitle());
        }

        if (dto.getPrice() != null) {
            entity.setPrice(dto.getPrice());
        }

        if (dto.getDescription() != null) {
            entity.setDescription(dto.getDescription());
        }
    }
}
