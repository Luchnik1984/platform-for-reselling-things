package ru.skypro.homework.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.ads.Ad;
import ru.skypro.homework.dto.ads.Ads;
import ru.skypro.homework.dto.ads.CreateOrUpdateAd;
import ru.skypro.homework.dto.ads.ExtendedAd;
import ru.skypro.homework.exceptions.AccessDeniedException;
import ru.skypro.homework.exceptions.AdNotFoundException;
import ru.skypro.homework.service.AdService;

import javax.validation.Valid;

/**
 * Контроллер для обработки операций с объявлениями.
 * Предоставляет полный набор CRUD-операций, а также специализированные
 * эндпоинты для работы с изображениями и для получения персональных объявлений.
 *
 <p>Архитектура: Контроллер делегирует бизнес-логику {@link AdService},
 * сосредотачиваясь только на:
 * <ul>
 *   <li>Валидации входящих данных (аннотации {@code @Valid})</li>
 *   <li>Извлечении параметров из запроса ({@code @PathVariable}, {@code @RequestPart})</li>
 *   <li>Формировании HTTP-ответов с правильными статусами</li>
 *   <li>Передаче контекста аутентификации в сервисный слой</li>
 * </ul>
 *
 * <p>Проверка прав доступа:
 * <ul>
 *   <li>USER: может управлять только своими объявлениями</li>
 *   <li>ADMIN: может управлять всеми объявлениями</li>
 *   <li>Проверка выполняется на уровне сервиса через {@code @PreAuthorize}</li>
 * </ul>
 *
 * @see AdService сервис, реализующий бизнес-логику операций с объявлениями
 * @see Ad DTO для краткой информации об объявлении
 * @see ExtendedAd DTO для полной информации об объявлении
 * @see Ads DTO-обёртка для списка объявлений
 * @see CreateOrUpdateAd DTO для получения данных от клиента при создании/обновлении
 */
@Slf4j
@CrossOrigin(value = "http://localhost:3000")
@RestController
@RequestMapping("/ads")
@RequiredArgsConstructor
@Tag(name = "Объявления", description = "API для CRUD операций с объявлениями")

public class AdController {

    private final AdService adService;

    /**
     * Получение списка всех объявлений.
     * Эндпоинт доступен для всех аутентифицированных пользователей.
     * На первом этапе возвращает пустой список с нулевым счётчиком.
     *
     * <p>Особенности реализации:
     * <ul>
     *   <li>Использует {@link AdService#getAllAds()} для получения данных</li>
     *   <li>Возвращает обёртку {@link Ads} с общим количеством и списком</li>
     *   <li>Не требует параметров аутентификации - открыт для всех авторизованных</li>
     * </ul>
     *
     * <p>Соответствие OpenAPI:
     * <ul>
     *   <li>Метод: GET</li>
     *   <li>URL: /ads</li>
     *   <li>Ответ: 200 OK с {@link Ads}</li>
     *   <li>Аутентификация: требуется (иначе 401)</li>
     * </ul>
     *
     * @return {@link ResponseEntity} со статусом 200 (OK) и DTO {@link Ads},
     * содержащего список объявлений и общее количество.
     */
    @Operation(
            summary = "Получение всех объявлений",
            description = "Возвращает список всех объявлений, размещённых на платформе. " +
                    "Ответ включает общее количество и массив объявлений в кратком формате."+
                    "Требуется аутентификация."
    )
    @ApiResponse(
            responseCode = "200",
            description = "OK",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = Ads.class)
            )
    )
    @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - требуется аутентификация",
            content = @Content(schema = @Schema(hidden = true))
    )
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Ads getAllAds() {
        log.info("Запрос получения всех объявлений");

        return adService.getAllAds();
    }

    /**
     * Получение полной информации об объявлении по его уникальному идентификатору (ID).
     * Эндпоинт доступен для всех аутентифицированных пользователей.
     * На первом этапе возвращает тестовые данные.
     * В дальнейшем будет выполняться поиск объявления в БД по ID.
     *
     * <p>Особенности реализации:
     * <ul>
     *   <li>Использует {@link AdService#getAd(Integer)} для получения данных</li>
     *   <li>Возвращает расширенное DTO {@link ExtendedAd} с контактной информацией автора</li>
     *   <li>Автоматически преобразует исключения в HTTP статусы</li>
     * </ul>
     *
     * <p>Обработка ошибок:
     * <ul>
     *   <li>404 Not Found: если объявление с указанным ID не существует</li>
     *   <li>401 Unauthorized: если пользователь не аутентифицирован</li>
     * </ul>
     *
     * @param id (path variable) - числовой идентификатор объявления, передаваемый в пути URL.
     *           Аннотация {@link PathVariable} извлекает значение из сегмента пути {id}.
     * @return DTO {@link ExtendedAd}, содержащего детальную информацию об объявлении.
     * @throws AdNotFoundException если объявление не найдено (-> 404)
     */
    @Operation(
            summary = "Получение информации об объявлении",
            description = "Возвращает полную, детализированную информацию об объявлении " +
                    "по его идентификатору (ID). Включает контактные данные автора."+
                    "Требуется аутентификация."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OK",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ExtendedAd.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - требуется аутентификация",
                    content = @Content(schema = @Schema(hidden = true))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Not Found - объявление с указанным ID не найдено",
                    content = @Content(schema = @Schema(hidden = true))
            )
    })
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ExtendedAd getAd(@PathVariable("id") Integer id) {
        log.info("Запрос получения объявления для ID = {}", id);

        return adService.getAd(id);
    }

    /**
     * Получение списка объявлений, созданных текущим пользователем.
     * Используется для реализации раздела "Мои объявления".
     *
     * <p>Особенности реализации:
     * <ul>
     *   <li>Использует {@link AdService#getUserAds(Authentication)} с фильтрацией по автору</li>
     *   <li>Автор определяется автоматически из {@link Authentication}</li>
     *   <li>Возвращает тот же формат, что и {@link #getAllAds()}, но с фильтрацией</li>
     * </ul>
     *
     * @param authentication объект аутентификации Spring Security,
     *  содержащий данные текущего пользователя
     * @return {@link Ads} с объявлениями текущего пользователя.
     */
    @Operation(
            summary = "Получение объявлений авторизованного пользователя",
            description = "Возвращает список объявлений, созданных текущим аутентифицированным пользователем. " +
                    "Используется для отображения раздела 'Мои объявления'."+
                    "Автор определяется автоматически из данных аутентификации."
    )
    @ApiResponse(
            responseCode = "200",
            description = "OK",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = Ads.class)
            )
    )
    @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - требуется аутентификация",
            content = @Content(schema = @Schema(hidden = true))
    )
    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    public Ads getAdsMe(Authentication authentication) {
        log.info("Запрос для получения объявлений текущего пользователя: {}", authentication.getName());

        return adService.getUserAds(authentication);
    }

    /**
     * Создание нового объявления.
     * Эндпоинт доступен для всех аутентифицированных пользователей.
     * Принимает данные в формате multipart/form-data
     *
     * <p>Особенности реализации:
     * <ul>
     *   <li>Использует {@link AdService#createAd(Authentication, CreateOrUpdateAd, MultipartFile)}</li>
     *   <li>Автор определяется автоматически из {@link Authentication}</li>
     *   <li>Валидирует входящие данные через {@code @Valid}</li>
     *   <li>Возвращает статус 201 Created с созданным ресурсом</li>
     * </ul>
     *
     * <p>Структура запроса (multipart/form-data):
     * <ul>
     *   <li>{@code properties}: JSON объект {@link CreateOrUpdateAd}</li>
     *   <li>{@code image}: файл изображения объявления</li>
     * </ul>
     *
     * !!! <strong>Внимание:</strong> Обработка изображения будет полностью реализована после ImageService.
     * На текущем этапе файл принимается, но не сохраняется.
     *
     * @param properties DTO {@link CreateOrUpdateAd}, содержащий заголовок, описание и цену.
     * @param image      Файл изображения для объявления.
     * @param authentication объект аутентификации текущего пользователя
     * @return {@link ResponseEntity} со статусом 201 (Created) и телом в виде DTO созданного объявления.
     */
    @Operation(
            summary = "Добавление объявления",
            description = "Создаёт новое объявление на платформе. " +
                    "Требует аутентификации. Принимает данные в форме multipart/form-data: " +
                    "JSON-объект 'properties' с информацией и файл 'image'."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Created - объявление успешно создано",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Ad.class)
                    )
            ),

            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(schema = @Schema(hidden = true))
            )
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Ad> addAd(
            @Valid @RequestPart("properties") CreateOrUpdateAd properties,
            @RequestPart("image") MultipartFile image,
            Authentication authentication) {
        log.info("Запрос для создания объявления. " +
                "Заголовок: {}, цена={}, размер файла: {} байт", properties.getTitle(),
                properties.getPrice(), image.getSize());


        Ad createdAd = adService.createAd(authentication, properties, image);

        log.info("Объявление успешно создано с ID: {}", createdAd.getPk());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAd);
    }

    /**
     * Частичное обновление информации об объявлении по его ID.
     * <p>Особенности реализации:
     * <ul>
     *   <li>Использует {@link AdService#updateAd(Integer, Authentication, CreateOrUpdateAd)}</li>
     *   <li>Проверка прав доступа выполняется на уровне сервиса</li>
     *   <li>Возвращает обновлённое объявление в кратком формате</li>
     * </ul>
     *
     * <p>Права доступа:
     * <ul>
     *   <li>Автор объявления может обновлять своё объявление</li>
     *   <li>ADMIN может обновлять любые объявления</li>
     *   <li>При попытке обновить чужое объявление возвращается 403 Forbidden</li>
     * </ul>
     *
     *
     * @param id         (path variable) ID объявления, которое требуется обновить.
     * @param updateData DTO {@link CreateOrUpdateAd} с новыми значениями полей.
     *                   Аннотация {@link RequestBody} указывает, что данные приходят в теле запроса в формате JSON.
     * @param authentication объект аутентификации текущего пользователя.
     * @return обновлённое объявление в формате {@link Ad}
     * @throws ru.skypro.homework.exceptions.AdNotFoundException если объявление не найдено (→ 404)
     * @throws ru.skypro.homework.exceptions.AccessDeniedException если нет прав доступа (→ 403)
     */
    @Operation(
            summary = "Обновление информации об объявлении",
            description = "Обновляет заголовок, цену или описание существующего объявления. " +
                    "Доступно только автору объявления или администратору." +
                    "Выполняет частичное обновление - обновляются только переданные поля."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OK - объявление успешно обновлено",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Ad.class)
                    )
            ),

            @ApiResponse(responseCode = "401",
                    description = "Unauthorized - требуется аутентификация",
                    content = @Content(schema = @Schema(hidden = true))
            ),
            @ApiResponse(responseCode = "403",
                    description = "Forbidden - попытка изменить чужое объявление",
                    content = @Content(schema = @Schema(hidden = true))
            ),
            @ApiResponse(responseCode = "404",
                    description = "Not Found - объявление не найдено",
                    content = @Content(schema = @Schema(hidden = true))
            )
    })
    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Ad updateAd(@PathVariable("id") Integer id,
                       @Valid @RequestBody CreateOrUpdateAd updateData,
                       Authentication authentication) {
        log.info("Запрос для обновления объявления с ID={}пользователем: {}",
                id, authentication.getName());
        log.debug("Обновляемые данные: заголовок='{}', цена={}",
                updateData.getTitle(), updateData.getPrice());

        return adService.updateAd(id, authentication, updateData);
    }

    /**
     * Обновляет (заменяет) изображение существующего объявления.
     * Является заглушкой до реализации ImageService.
     *
     * <p>Архитектура (по плану реализации):
     * <ol>
     *   <li><strong>Текущая фаза:</strong> Заглушка, возвращает пустой массив байтов</li>
     *   <li><strong>Будущая фаза:</strong> Будет использовать {@code ImageService.uploadAdImage()}</li>
     * </ol>
     *
     * <p>Проверка прав доступа (будет реализована в ImageService.):
     * <ul>
     *   <li>USER: может обновлять изображение только своих объявлений</li>
     *   <li>ADMIN: может обновлять изображение любых объявлений</li>
     * </ul>
     *
     * <p>Особенность OpenAPI: Данный эндпоинт должен возвращать байты обновлённого изображения
     * в формате {@code application/octet-stream}.
     *
     * @param id идентификатор объявления, изображение которого требуется обновить
     * @param image новый файл изображения в формате multipart/form-data
     * @param authentication объект аутентификации Spring Security,
     *                     содержащий данные текущего пользователя
     * @return {@link ResponseEntity} со статусом 200 (OK) и массивом байтов изображения
     *         в соответствии с OpenAPI спецификацией
     */
    @Operation(
            summary = "Обновление картинки объявления",
            description = "Заменяет изображение существующего объявления на новое. " +
                    "Доступно только автору объявления или администратору. " +
                    "Возвращает загруженное изображение в виде массива байтов " +
                    "в соответствии с OpenAPI спецификацией. " +
                    "На текущем этапе является заглушкой."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OK - изображение успешно обновлено (заглушка)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE,
                            schema = @Schema(type = "string", format = "byte")
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - требуется аутентификация",
                    content = @Content(schema = @Schema(hidden = true))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - попытка изменить чужое объявление",
                    content = @Content(schema = @Schema(hidden = true))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Not Found - объявление не найдено",
                    content = @Content(schema = @Schema(hidden = true))
            )
    })
    @PatchMapping(path = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<byte[]> updateAdImage(@PathVariable("id") Integer id,
                                                @RequestPart("image") MultipartFile image,
                                                Authentication authentication) {

        String email = authentication.getName();
        log.info("Запрос обновления изображения объявления ID: {} пользователем: {}", id, email);

        log.debug("Детали файла от пользователя {}: имя='{}', размер={} байт, тип={}",
                email,
                image.getOriginalFilename(),
                image.getSize(),
                image.getContentType());

        /* TODO: US9.2 - Реализовать полную логику через ImageService
         Получить объявление через AdService (с проверкой существования)
         Проверить права доступа:
            - USER: adEntity.getAuthor().getEmail().equals(email)
            - ADMIN: authentication.getAuthorities() содержит ROLE_ADMIN
         Вызвать ImageService.uploadAdImage(adEntity, image)
         Получить байты сохранённого изображения
         Вернуть байты с правильными HTTP-заголовками
         */

        // Временная заглушка: возвращаем пустой массив байтов
        byte[] emptyBytes = new byte[0];

        log.info("Изображение для объявления ID: {} принято (заглушка, US8.2). " +
                "Полная реализация будет в US9.2.", id);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", "inline")
                .body(emptyBytes);
    }

    /**
     * Удаление объявления по его ID.
     * Выполняет каскадное удаление всех связанных комментариев.
     *
     * <p>Особенности реализации:
     * <ul>
     *   <li>Использует {@link AdService#deleteAd(Integer, Authentication)}</li>
     *   <li>Проверка прав доступа выполняется на уровне сервиса</li>
     *   <li>Возвращает статус 204 No Content без тела ответа</li>
     *   <li>Комментарии удаляются каскадно благодаря настройкам сущности</li>
     * </ul>
     *
     * <p>Права доступа:
     * <ul>
     *   <li>Автор объявления может удалить своё объявление</li>
     *   <li>ADMIN может удалить любое объявление</li>
     *   <li>При попытке удалить чужое объявление возвращается 403 Forbidden</li>
     * </ul>
     *
     * @param id идентификатор удаляемого объявления
     * @param authentication объект аутентификации текущего пользователя
     * @throws AdNotFoundException если объявление не найдено (→ 404)
     * @throws AccessDeniedException если нет прав доступа (→ 403)
     */
    @Operation(
            summary = "Удаление объявления",
            description = "Полностью удаляет объявление с платформы. " +
                    "Доступно только автору объявления или администратору. " +
                    "Успешный ответ не содержит тела (204 No Content)."+
                    "Все комментарии к объявлению удаляются каскадно."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204",
                    description = "No Content - объявление успешно удалено"
            ),
            @ApiResponse(responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(schema = @Schema(hidden = true))
            ),
            @ApiResponse(responseCode = "403",
                    description = "Forbidden - попытка удалить чужое объявление",
                    content = @Content(schema = @Schema(hidden = true))
            ),
            @ApiResponse(responseCode = "404",
                    description = "Not Found - объявление не найдено",
                    content = @Content(schema = @Schema(hidden = true))
            )
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAd(@PathVariable("id") Integer id,
                          Authentication authentication) {
        log.info("Запрос удаления объявления ID: {} пользователем: {}",
                id, authentication.getName());

        adService.deleteAd(id, authentication);

        log.info("Объявление ID: {} успешно удалено", id);

    }

}
