package ru.skypro.homework.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.Ad;
import ru.skypro.homework.dto.Ads;
import ru.skypro.homework.dto.CreateOrUpdateAd;
import ru.skypro.homework.dto.ExtendedAd;

import java.util.List;
import java.util.Random;

/**
 * Контроллер для обработки операций с объявлениями.
 * Предоставляет полный набор CRUD-операций, а также специализированные
 * эндпоинты для работы с изображениями и для получения персональных объявлений.
 * На первом этапе это "заглушки".
 *
 * @see Ad DTO для краткой информации об объявлении
 * @see ExtendedAd DTO для полной информации об объявлении
 * @see Ads DTO-обёртка для списка объявлений
 * @see CreateOrUpdateAd DTO для получения данных от клиента при создании/обновлении
 */
@Slf4j
@CrossOrigin(value = "http://localhost:3000")
@RestController
@RequestMapping("/ads")
@Tag(name = "Объявления", description = "API для CRUD операций с объявлениями")

public class AdController {

    /**
     * Получение списка всех объявлений.
     * Эндпоинт доступен для всех аутентифицированных пользователей.
     * На первом этапе возвращает пустой список с нулевым счётчиком.
     *
     * @return {@link ResponseEntity} со статусом 200 (OK) и DTO {@link Ads},
     *         содержащего пустой список объявлений и общее количество, равное 0.
     */
    @Operation(
            summary = "Получение всех объявлений",
            description = "Возвращает список всех объявлений, размещённых на платформе. " +
                    "Ответ включает общее количество и массив объявлений в кратком формате."
    )
    @ApiResponse(
            responseCode = "200",
            description = "OK",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = Ads.class)
            )
    )
    @GetMapping
    public ResponseEntity<Ads> getAllAds() {
        log.info("Был вызван метод контроллера getAllAds для получения всех объявлений");
        Ads emptyAdsList = new Ads();
        emptyAdsList.setCount(0);
        emptyAdsList.setResults(List.of());

        return ResponseEntity.ok(emptyAdsList);
    }

    /**
     * Получение полной информации об объявлении по его уникальному идентификатору (ID).
     * Эндпоинт доступен для всех аутентифицированных пользователей.
     * На первом этапе возвращает тестовые данные.
     * В дальнейшем будет выполняться поиск объявления в БД по ID.
     *
     * @param id (path variable) - числовой идентификатор объявления, передаваемый в пути URL.
     *           Аннотация {@link PathVariable} извлекает значение из сегмента пути {id}.
     * @return {@link ResponseEntity} со статусом 200 (OK) и DTO {@link ExtendedAd},
     *         содержащего детальную информацию об объявлении.
     *         ! В будущей реализации:
     *         Если объявление не найдено, будет возвращён статус 404 (Not Found).
     */
    @Operation(
            summary = "Получение информации об объявлении",
            description = "Возвращает полную, детализированную информацию об объявлении " +
                    "по его идентификатору (ID). Включает контактные данные автора."
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
                    responseCode = "404",
                    description = "Not Found",
                    content = @Content(schema = @Schema(hidden = true))
            )
    })
    @GetMapping("/{id}") // Обрабатывает GET запросы по пути '/ads/{id}', где {id} - переменная пути
    public ResponseEntity<ExtendedAd> getAd(@PathVariable("id") Integer id) { /// @PathVariable связывает {id} из URL с параметром метода
        log.info("Был вызван метод контроллера getAd для получения объявления с ID = {}", id);

        // Заглушка создаём и возвращаем объект ExtendedAd с тестовыми данными.
        // В реальной реализации здесь будет поиск в базе данных по переданному 'id'.
        ExtendedAd extendedAdStub = new ExtendedAd();
        extendedAdStub.setPk(id); // Используем переданный ID для наглядности
        extendedAdStub.setTitle("Тестовое объявление");
        extendedAdStub.setPrice(9999);
        extendedAdStub.setDescription("Это детальное описание тестового объявления, созданного на этапе разработки.");
        extendedAdStub.setImage("/images/stub.jpg");
        extendedAdStub.setAuthorFirstName("Имя");
        extendedAdStub.setAuthorLastName("Фамилия");
        extendedAdStub.setEmail("stub@example.com");
        extendedAdStub.setPhone("+7 (000) 000-00-00");

        return ResponseEntity.ok(extendedAdStub);
    }

    /**
     * Получение списка объявлений, созданных текущим пользователем.
     * Эндпоинт доступен для всех аутентифицированных пользователей.
     * Является частным случаем {@link #getAllAds()}, но с фильтрацией по автору.
     * На первом этапе возвращает пустой список.
     * В дальнейшем будет выполняться запрос к БД с фильтром по ID текущего пользователя,
     * который можно получить из контекста безопасности.
     *
     * @return {@link ResponseEntity} со статусом 200 (OK) и DTO {@link Ads},
     *         содержащего пустой список объявлений.
     */
    @Operation(
            summary = "Получение объявлений авторизованного пользователя",
            description = "Возвращает список объявлений, созданных текущим аутентифицированным пользователем. " +
                    "Используется для отображения раздела 'Мои объявления'."
    )
    @ApiResponse(
            responseCode = "200",
            description = "OK",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = Ads.class)
            )
    )
    @GetMapping("/me")
    public ResponseEntity<Ads> getAdsMe() {
        log.info("Был вызван метод контроллера getAdsMe для получения объявлений текущего пользователя");
        // Заглушка - создаёт и возвращает пустой объект Ads.
        // В дальнейшем здесь должна быть логика извлечения ID текущего пользователя.
        Ads myEmptyAdsList = new Ads();
        myEmptyAdsList.setCount(0);
        myEmptyAdsList.setResults(List.of());

        return ResponseEntity.ok(myEmptyAdsList);
    }

    /**
     * Создание нового объявления.
     * Эндпоинт доступен для всех аутентифицированных пользователей.
     * Принимает данные в формате multipart/form-data: JSON с информацией об объявлении
     * и файл изображения.
     * На первом этапе это заглушка, которая игнорирует загруженный файл и входящие данные,
     * возвращая фиктивный объект созданного объявления со статусом 201 (Created).
     * В будущей реализации будет:
     * 1. Сохранение загруженного изображения.
     * 2. Извлечение ID текущего пользователя из контекста безопасности для установки автора.
     * 3. Сохранение информации об объявлении в БД.
     *
     * @param properties DTO {@link CreateOrUpdateAd}, содержащий заголовок, описание и цену.
     *                   Аннотация {@link RequestPart} связывает часть multipart-запроса с именем "properties"
     *                   (как указано в OpenAPI) с этим параметром. Spring автоматически преобразует JSON в объект.
     * @param image Файл изображения для объявления.
     *              Аннотация {@link RequestPart} связывает часть multipart-запроса с именем "image" с этим параметром.
     * @return {@link ResponseEntity} со статусом 201 (Created) и телом в виде DTO {@link Ad},
     *         содержащего данные созданного объявления (включая сгенерированный ID и ссылку на изображение).
     *         В заголовке 'Location' ответа в будущем должен быть URL созданного ресурса.
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
                    description = "Created",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Ad.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - неверный формат данных или файла",
                    content = @Content(schema = @Schema(hidden = true))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(schema = @Schema(hidden = true))
            )
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Ad> addAd(@RequestPart("properties") CreateOrUpdateAd properties,
                                    @RequestPart("image") MultipartFile image) {
        log.info("Был вызван метод контроллера addAd для создания объявления. " +
                "Заголовок: {}, размер файла: {} байт", properties.getTitle(), image.getSize());

        // Заглушка создаёт и возвращает объект Ad.
        // В дельнейшем здесь должна быть логика: сохранение файла, работа с БД.
        Ad createdAdStub = new Ad();
        // Генерируем фиктивный, случайный ID
        createdAdStub.setPk(new Random().nextInt(10000)+1);
        createdAdStub.setAuthor(1); // Фиктивный ID автора.
        createdAdStub.setTitle(properties.getTitle());
        createdAdStub.setPrice(properties.getPrice());
        // Фиктивная ссылка на изображение. В будущем - путь к сохранённому файлу.
        createdAdStub.setImage("/images/ads/" + createdAdStub.getPk() + "-stub.jpg");

        log.debug("Заглушка addAd вернёт объект: {}", createdAdStub);
        // Возвращаем статус 201 Created и созданный объект.
        // В дальнейшем возможно стоит добавить заголовок Location: /ads/{id}
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAdStub);
    }

    /**
     * Частичное обновление информации об объявлении по его ID.
     * Эндпоинт доступен только автору объявления или ADMIN
     * (проверка роли реализуется на следующих этапах).
     * Принимает DTO {@link CreateOrUpdateAd} в формате JSON, содержащее поля для обновления.
     * Метод PATCH позволяет обновить только переданные поля, оставляя остальные без изменений.
     * На первом этапе это заглушка, которая возвращает объект {@link Ad} с обновлёнными,
     * на основе входящих данных, полями title и price, но с фиктивными значениями
     * для остальных полей (pk, author, image).
     *
     * @param id (path variable) ID объявления, которое требуется обновить.
     * @param updateData DTO {@link CreateOrUpdateAd} с новыми значениями полей.
     *                   Аннотация {@link RequestBody} указывает, что данные приходят в теле запроса в формате JSON.
     * @return {@link ResponseEntity} со статусом 200 (OK) и DTO {@link Ad},
     *         содержащего данные обновлённого объявления.
     *         В случае попытки обновления несуществующего объявления (в будущем) будет возвращён 404.
     *         В случае попытки обновления чужого объявления (в будущем) будет возвращён 403.
     */
    @Operation(
            summary = "Обновление информации об объявлении",
            description = "Обновляет заголовок, цену или описание существующего объявления. " +
                    "Доступно только автору объявления или администратору."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OK",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Ad.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden - попытка изменить чужое объявление", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Not Found - объявление не найдено", content = @Content(schema = @Schema(hidden = true)))
    })
    @PatchMapping("/{id}")
    public ResponseEntity<Ad> updateAd(@PathVariable("id") Integer id,
                                       @RequestBody CreateOrUpdateAd updateData) {
        log.info("Был вызван метод updateAd для обновления объявления с ID={}. Новые данные: {}", id, updateData);

        // Заглушка создаёт объект Ad, имитируя успешно обновлённое объявление.
        Ad updatedAdStub = new Ad();
        updatedAdStub.setPk(id);
        updatedAdStub.setAuthor(1); // Фиктивный ID.
        updatedAdStub.setTitle(updateData.getTitle() != null ? updateData.getTitle() : "[Заголовок не изменялся]");
        updatedAdStub.setPrice(updateData.getPrice() != null ? updateData.getPrice() : 0);
        // Поле 'description' отсутствует в DTO Ad, поэтому не используется здесь.
        // Оно будет обновлено в сущности и отобразится в ExtendedAd.
        updatedAdStub.setImage("/images/ads/" + id + ".jpg"); // Фиктивная ссылка на изображение

        log.debug("Заглушка updateAd вернёт объект: {}", updatedAdStub);
        return ResponseEntity.ok(updatedAdStub);
    }

    /**
     * Обновление (замена) изображения для существующего объявления.
     * Эндпоинт доступен только автору объявления или ADMIN.
     * Принимает новый файл изображения в формате multipart/form-data.
     * В спецификации OpenAPI указано, что успешный ответ может содержать сами байты изображения
     * (content-type: application/octet-stream), но на первом этапе (заглушка) просто
     * вернём статус 200 OK, подтверждающий успешную загрузку.
     * На этапе заглушки метод логирует факт вызова и возвращает успешный статус.
     *
     * @param id (path variable) ID объявления, для которого обновляется изображение.
     * @param image Новый файл изображения, передаваемый как часть multipart-запроса.
     * @return {@link ResponseEntity} со статусом 200 (OK) и пустым телом.
     *         В будущем, при реализации, может возвращать обновлённые байты изображения
     *         или ссылку на него.
     */
    @Operation(
            summary = "Обновление картинки объявления",
            description = "Заменяет изображение существующего объявления на новое. " +
                    "Доступно только автору объявления или администратору."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK - изображение успешно обновлено"),
            @ApiResponse(responseCode = "400", description = "Bad Request - неверный формат файла", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden - попытка изменить чужое объявление", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Not Found - объявление не найдено", content = @Content(schema = @Schema(hidden = true)))
    })
    @PatchMapping(path = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updateAdImage(@PathVariable("id") Integer id,
                                              @RequestPart("image") MultipartFile image) {
        log.info("Был вызван метод updateAdImage для обновления изображения объявления ID={}. " +
                "Имя файла: {}, размер: {} байт", id, image.getOriginalFilename(), image.getSize());

        // ПоказЗаглушка. В дельнейшем нужно будет реализовать:
        // 1. Проверку прав доступа к объявлению с ID = id.
        // 2. Удаление старого файла изображения (если есть).
        // 3. Сохранение нового файла 'image'.
        // 4. Обновление ссылки на изображение в сущности 'Ad' в БД.
        log.debug("Заглушка: файл '{}' для объявления ID={} принят и условно сохранён.",
                image.getOriginalFilename(), id);

        // Возвращаем статус 200 OK, подтверждающий успешный приём файла.
        // Можно также вернуть ResponseEntity.ok().build();
        return ResponseEntity.ok().build();
    }

    /**
     * Удаление объявления по его ID.
     * Эндпоинт доступен только автору объявления или ADMIN
     * (проверка роли реализуется на следующих этапах).
     * При успешном удалении возвращает статус 204 (No Content) без тела ответа.
     * На первом этапе это заглушка, которая имитирует успешное удаление.
     *
     * @param id (path variable) ID объявления, которое требуется удалить.
     * @return {@link ResponseEntity} со статусом 204 (No Content) и пустым телом.
     *         В случае попытки удаления несуществующего объявления (в будущем) будет возвращён 404.
     *         В случае попытки удаления чужого объявления (в будущем) будет возвращён 403.
     */
    @Operation(
            summary = "Удаление объявления",
            description = "Полностью удаляет объявление с платформы. " +
                    "Доступно только автору объявления или администратору. " +
                    "Успешный ответ не содержит тела (204 No Content)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "No Content - объявление успешно удалено"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden - попытка удалить чужое объявление", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Not Found - объявление не найдено", content = @Content(schema = @Schema(hidden = true)))
    })
    @DeleteMapping("/{id}") // Обрабатывает DELETE запросы на путь '/ads/{id}'
    public ResponseEntity<Void> deleteAd(@PathVariable("id") Integer id) {
        log.info("Был вызван метод deleteAd для удаления объявления с ID={}", id);

        // Заглушка.
        // Пока просто логируем факт "удаления" и возвращаем успешный статус.
        log.debug("Заглушка: объявление с ID={} помечено как удалённое", id);

        return ResponseEntity.noContent().build();
    }

}
