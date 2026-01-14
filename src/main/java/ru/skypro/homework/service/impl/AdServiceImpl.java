package ru.skypro.homework.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import ru.skypro.homework.exceptions.AccessDeniedException;
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
    private final ImageServiceImpl imageService;

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

        AdEntity savedEntity = adRepository.save(adEntity);
        Ad createdAd = adMapper.toDto(savedEntity);

        imageService.uploadAdImage(savedEntity.getId(), image);

        log.info("Создано объявление ID: {} пользователем {}", savedEntity.getId(), email);
        return createdAd;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Проверка прав доступа:
     * <ol>
     *   <li>Находит объявление по ID</li>
     *   <li>USER может обновлять только свои объявления</li>
     *   <li>ADMIN может обновлять любые объявления</li>
     *   <li>Обновляет только переданные поля (частичное обновление)</li>
     * </ol>
     *
     * <p>Безопасность: Использует {@link SecurityUtils#isAdmin(Authentication)} для проверки роли
     * и явную проверку авторства {@code adEntity.getAuthor().getEmail().equals(email)}
     */
    @Override
    @Transactional
    public Ad updateAd(Integer id, Authentication authentication, CreateOrUpdateAd updateAd) {
        String email = authentication.getName();
        log.debug("Обновление объявления ID: {} пользователем: {}", id, email);

        AdEntity adEntity = adRepository.findById(id)
                .orElseThrow(() -> new AdNotFoundException(id));

        // Проверка прав доступа с использованием SecurityUtils
        boolean isAdmin = SecurityUtils.isAdmin(authentication);
        boolean isAuthor = adEntity.getAuthor().getEmail().equals(email);

        if (!isAdmin && !isAuthor) {
            log.warn("Попытка обновления чужого объявления ID: {} пользователем: {}", id, email);
            throw new AccessDeniedException("ad", id);
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
     * <p>Проверка прав доступа:
     * <ol>
     *   <li>USER может удалять только свои объявления</li>
     *   <li>ADMIN может удалять любые объявления</li>
     *   <li>При попытке удалить чужое объявление - {@link AccessDeniedException}</li>
     * </ol>
     *
     * <p>Каскадное удаление: При удалении объявления
     * автоматически удаляются все связанные комментарии
     * благодаря настройке {@code cascade = CascadeType.ALL}
     * в сущности {@link AdEntity}.
     *
     * <p>Безопасность: Использует {@link SecurityUtils#isAdmin(Authentication)} для проверки роли
     * и явную проверку авторства {@code adEntity.getAuthor().getEmail().equals(email)}
     */
    @Override
    @Transactional
    public void deleteAd(Integer id, Authentication authentication) {
        String email = authentication.getName();
        log.debug("Удаление объявления ID: {} пользователем: {}", id, email);

        AdEntity adEntity = adRepository.findById(id)
                .orElseThrow(() -> new AdNotFoundException(id));

        // Проверка прав доступа с использованием SecurityUtils
        boolean isAdmin = SecurityUtils.isAdmin(authentication);
        boolean isAuthor = adEntity.getAuthor().getEmail().equals(email);

        if (!isAdmin && !isAuthor) {
            log.warn("Попытка удаления чужого объявления ID: {} пользователем: {}", id, email);
            throw new AccessDeniedException("ad", id);
        }

        adRepository.delete(adEntity);
        log.info("Объявление ID: {} удалено пользователем {}", id, email);
    }

}
