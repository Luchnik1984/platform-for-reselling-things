package ru.skypro.homework.service;

import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.ads.Ad;
import ru.skypro.homework.dto.ads.Ads;
import ru.skypro.homework.dto.ads.CreateOrUpdateAd;
import ru.skypro.homework.dto.ads.ExtendedAd;
import ru.skypro.homework.exceptions.AccessDeniedException;
import ru.skypro.homework.exceptions.AdNotFoundException;

/**
 * Сервис для управления объявлениями.
 * Обеспечивает бизнес-логику CRUD-операций с объявлениями,
 * включая проверку прав доступа и работу с изображениями.
 *
 * <p>Права доступа:
 * <ul>
 *   <li>USER: может управлять только своими объявлениями</li>
 *   <li>ADMIN: может управлять всеми объявлениями</li>
 * </ul>
 *
 * @see ru.skypro.homework.controller.AdController
 * @see ru.skypro.homework.dto.ads.Ad
 * @see ru.skypro.homework.dto.ads.ExtendedAd
 */
public interface AdService {

    /**
     * Получает список всех объявлений.
     * Доступно всем аутентифицированным пользователям.
     *
     * @return список объявлений с общим количеством
     */
    Ads getAllAds();

    /**
     * Получает полную информацию об объявлении по его ID.
     * Доступно всем аутентифицированным пользователям.
     *
     * @param id идентификатор объявления
     * @return полная информация об объявлении
     * @throws AdNotFoundException если объявление не найдено
     */
    ExtendedAd getAd(Integer id);

    /**
     * Получает объявления текущего аутентифицированного пользователя.
     * Используется для раздела "Мои объявления".
     *
     * @param authentication объект аутентификации Spring Security
     * @return список объявлений текущего пользователя
     */
    Ads getUserAds(Authentication authentication);

    /**
     * Создаёт новое объявление для текущего пользователя.
     * Автоматически устанавливает автора из данных аутентификации.
     *
     * @param authentication объект аутентификации
     * @param createAd DTO с данными объявления
     * @param image файл изображения объявления
     * @return созданное объявление в кратком формате
     * @throws IllegalArgumentException если данные некорректны
     */
    Ad createAd(Authentication authentication, CreateOrUpdateAd createAd, MultipartFile image);

    /**
     * Обновляет существующее объявление.
     * Проверяет права доступа: только автор или ADMIN.
     *
     * @param id идентификатор объявления
     * @param authentication объект аутентификации
     * @param updateAd DTO с обновлёнными данными
     * @return обновлённое объявление в кратком формате
     * @throws AdNotFoundException если объявление не найдено
     * @throws AccessDeniedException если пользователь не имеет прав
     */
    Ad updateAd(Integer id, Authentication authentication, CreateOrUpdateAd updateAd);

    /**
     * Удаляет объявление.
     * Проверяет права доступа: только автор или ADMIN.
     * Выполняет каскадное удаление комментариев.
     *
     * @param id идентификатор объявления
     * @param authentication объект аутентификации
     * @throws AdNotFoundException если объявление не найдено
     * @throws AccessDeniedException если пользователь не имеет прав
     */
    void deleteAd(Integer id, Authentication authentication);

}

