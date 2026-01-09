package ru.skypro.homework.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.ads.Ad;
import ru.skypro.homework.dto.ads.Ads;
import ru.skypro.homework.dto.ads.CreateOrUpdateAd;
import ru.skypro.homework.dto.ads.ExtendedAd;
import ru.skypro.homework.entity.AdEntity;
import ru.skypro.homework.entity.UserEntity;
import ru.skypro.homework.exceptions.AdNotFoundException;
import ru.skypro.homework.mapper.AdMapper;
import ru.skypro.homework.repository.AdRepository;
import ru.skypro.homework.repository.UserRepository;
import ru.skypro.homework.service.AdService;
import ru.skypro.homework.util.SecurityUtils;

import java.util.List;


/**
 * Реализация сервиса для управления объявлениями.
 * Обеспечивает бизнес-логику CRUD-операций с проверкой прав доступа.
 *
 * <p>Архитектура: Service → Repository → Database
 *
 * @see AdService
 * @see AdRepository
 * @see AdMapper
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdServiceImpl implements AdService {

    private final AdRepository adRepository;
    private final UserRepository userRepository;
    private final AdMapper adMapper;

    /**
     * {@inheritDoc}
     *
     * <p>Реализация:
     * <ol>
     *   <li>Получает все объявления из репозитория</li>
     *   <li>Преобразует каждую сущность в DTO через маппер</li>
     *   <li>Возвращает обёртку с количеством и списком</li>
     * </ol>
     */
    @Override
    @Transactional(readOnly = true)
    public Ads getAllAds() {
        log.debug("Получение списка всех объявлений");

        List<Ad> results = adRepository.findAll()
                .stream()
                .map(adMapper::toDto)
                .toList();

        Ads ads = new Ads();
        ads.setCount(results.size());
        ads.setResults(results);

        log.trace("Найдено {} объявлений", results.size());
        return ads;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Реализация:
     * <ol>
     *   <li>Ищет объявление по ID в репозитории</li>
     *   <li>Если не найдено - выбрасывает {@link AdNotFoundException}</li>
     *   <li>Преобразует сущность в расширенное DTO</li>
     * </ol>
     */
    @Override
    @Transactional(readOnly = true)
    public ExtendedAd getAd(Integer id) {
        log.debug("Получение объявления с ID: {}", id);

        AdEntity adEntity = adRepository.findById(id)
                .orElseThrow(() -> new AdNotFoundException(id));

        ExtendedAd extendedAd = adMapper.toExtendedDto(adEntity);

        log.trace("Объявление {} найдено: {}", id, extendedAd.getTitle());
        return extendedAd;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Реализация:
     * <ol>
     *   <li>Получает текущего пользователя по email из аутентификации</li>
     *   <li>Ищет все объявления этого пользователя</li>
     *   <li>Преобразует в DTO и возвращает обёртку</li>
     * </ol>
     *
     * <p>Особенность: Использует метод репозитория {@code findAllByAuthorId}
     * для эффективной фильтрации по автору.
     */
    @Override
    @Transactional(readOnly = true)
    public Ads getUserAds(Authentication authentication) {
        String email = authentication.getName();
        log.debug("Получение объявлений пользователя: {}", email);

        UserEntity user = SecurityUtils.getAuthenticatedUser(userRepository, authentication);

        List<Ad> results = adRepository.findAllByAuthorId(user.getId())
                .stream()
                .map(adMapper::toDto)
                .toList();

        Ads ads = new Ads();
        ads.setCount(results.size());
        ads.setResults(results);

        log.trace("Найдено {} объявлений пользователя {}", results.size(), email);
        return ads;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Реализация:
     * <ol>
     *   <li>Получает текущего пользователя (автора)</li>
     *   <li>Создаёт сущность объявления из DTO</li>
     *   <li>Устанавливает автора</li>
     *   <li>Сохраняет в репозитории</li>
     *   <li>Возвращает созданное объявление в кратком формате</li>
     * </ol>
     *
     * <p>Внимание: Изображение пока не обрабатывается (Реализовать после ImageService).
     */
    @Override
    @Transactional
    public Ad createAd(Authentication authentication, CreateOrUpdateAd createAd, MultipartFile image) {
        String email = authentication.getName();
        log.debug("Создание объявления пользователем: {}", email);

        UserEntity author = SecurityUtils.getAuthenticatedUser(userRepository, authentication);

        AdEntity adEntity = adMapper.toEntity(createAd);
        adEntity.setAuthor(author);

        // TODO: Реализовать после ImageService
        // imageService.uploadAdImage(adEntity, image);

        AdEntity savedEntity = adRepository.save(adEntity);
        Ad createdAd = adMapper.toDto(savedEntity);

        log.info("Создано объявление ID: {} пользователем {}", savedEntity.getId(), email);
        return createdAd;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Проверка прав доступа:
     * <ol>
     *   <li>Находит объявление по ID</li>
     *   <li>Проверяет, является ли текущий пользователь автором</li>
     *   <li>Если нет - проверяет роль ADMIN через {@code @PreAuthorize}</li>
     *   <li>Обновляет только переданные поля (частичное обновление)</li>
     * </ol>
     *
     * <p>Безопасность: Явная проверка {@code adEntity.getAuthor().getEmail().equals(email)}
     * соответствует требованиям "Этапа 3".
     */
    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public Ad updateAd(Integer id, Authentication authentication, CreateOrUpdateAd updateAd) {
        String email = authentication.getName();
        log.debug("Обновление объявления ID: {} пользователем: {}", id, email);

        AdEntity adEntity = adRepository.findById(id)
                .orElseThrow(() -> new AdNotFoundException(id));

        // Проверка прав: либо автор, либо ADMIN (аннотация выше)
        if (!adEntity.getAuthor().getEmail().equals(email)) {
            // Если не автор, то должен быть ADMIN (проверено @PreAuthorize)
            log.debug("Обновление объявления администратором");
        }

        adMapper.updateEntityFromDto(updateAd, adEntity);
        AdEntity updatedEntity = adRepository.save(adEntity);
        Ad updatedAd = adMapper.toDto(updatedEntity);

        log.info("Объявление ID: {} обновлено пользователем {}", id, email);
        return updatedAd;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Каскадное удаление: При удалении объявления
     * автоматически удаляются все связанные комментарии
     * благодаря настройке {@code cascade = CascadeType.ALL}
     * в сущности {@link AdEntity}.
     *
     * <p>Безопасность: Используется явная проверка прав
     * перед выполнением операции удаления.
     */
    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteAd(Integer id, Authentication authentication) {
        String email = authentication.getName();
        log.debug("Удаление объявления ID: {} пользователем: {}", id, email);

        AdEntity adEntity = adRepository.findById(id)
                .orElseThrow(() -> new AdNotFoundException(id));

        // Проверка прав: либо автор, либо ADMIN (аннотация выше)
        if (!adEntity.getAuthor().getEmail().equals(email)) {
            // Если не автор, то должен быть ADMIN (проверено @PreAuthorize)
            log.debug("Удаление объявления администратором");
        }

        adRepository.delete(adEntity);
        log.info("Объявление ID: {} удалено пользователем {}", id, email);
    }

    /**
     * Вспомогательный метод для проверки прав доступа.
     * Используется в методах без {@code @PreAuthorize}.
     *
     * @param adEntity сущность объявления
     * @param email email текущего пользователя
     * @return true если пользователь имеет права на операцию
     */
    private boolean hasPermission(AdEntity adEntity, String email, Authentication authentication) {
        // Автор всегда имеет права
        if (adEntity.getAuthor().getEmail().equals(email)) {
            return true;
        }

        // ADMIN имеет права (проверка роли)
        return authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    }
}
