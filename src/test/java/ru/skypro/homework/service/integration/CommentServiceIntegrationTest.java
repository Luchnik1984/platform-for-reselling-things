package ru.skypro.homework.service.integration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import ru.skypro.homework.AbstractIntegrationTest;
import ru.skypro.homework.dto.comments.Comment;
import ru.skypro.homework.dto.comments.Comments;
import ru.skypro.homework.dto.comments.CreateOrUpdateComment;
import ru.skypro.homework.entity.AdEntity;
import ru.skypro.homework.entity.CommentEntity;
import ru.skypro.homework.entity.UserEntity;
import ru.skypro.homework.enums.Role;
import ru.skypro.homework.exceptions.AccessDeniedException;
import ru.skypro.homework.exceptions.AdNotFoundException;
import ru.skypro.homework.exceptions.CommentNotFoundException;
import ru.skypro.homework.repository.AdRepository;
import ru.skypro.homework.repository.CommentRepository;
import ru.skypro.homework.repository.UserRepository;
import ru.skypro.homework.service.CommentService;
import ru.skypro.homework.service.impl.CommentServiceImpl;
import ru.skypro.homework.util.SecurityUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

/**
 * Интеграционные тесты для {@link ru.skypro.homework.service.CommentService}.
 * <p>
 * Проверяет бизнес-логику работы с комментариями в полной среде с БД,
 * включая проверку прав доступа для ролей USER и ADMIN.
 * </p>
 *
 * <p><b>Тестовые сценарии:</b>
 * <ul>
 *   <li>Получение комментариев объявления</li>
 *   <li>Добавление новых комментариев</li>
 *   <li>Обновление комментариев (своих и администратором)</li>
 *   <li>Удаление комментариев (своих и администратором)</li>
 *   <li>Проверка прав доступа для чужого контента</li>
 *   <li>Обработка ошибок для несуществующих ресурсов</li>
 * </ul>
 * </p>
 *
 * <p><b>Особенности реализации:</b>
 * <ul>
 *   <li>Использует Testcontainers с PostgreSQL для изолированной БД</li>
 *   <li>Каждый тест выполняется в отдельной транзакции</li>
 *   <li>Проверяет как успешные сценарии, так и обработку исключений</li>
 *   <li>Тестирует работу {@link SecurityUtils}</li>
 * </ul>
 * </p>
 *
 * @see CommentService
 * @see CommentServiceImpl
 * @see AbstractIntegrationTest
 */

@Transactional
class CommentServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private CommentService commentService;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private AdRepository adRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private UserEntity user;

    private UserEntity admin;

    private AdEntity testAd;

    private Authentication userAuth;

    private Authentication adminAuth;

    private Authentication anotherUserAuth;

    /**
     * Настройка тестового окружения перед каждым тестом.
     * <p>
     * Выполняет:
     * <ol>
     *   <li>Очистку БД (благодаря {@code @Transactional})</li>
     *   <li>Создание тестовых пользователей (USER, ADMIN, другой USER)</li>
     *   <li>Создание тестового объявления</li>
     *   <li>Инициализацию объектов {@link Authentication}</li>
     * </ol>
     */
    @BeforeEach
    void setUp() {
        // Очистка БД перед каждым тестом (благодаря @Transactional)
        commentRepository.deleteAll();
        adRepository.deleteAll();
        userRepository.deleteAll();

        // Создание тестовых пользователей
        user = createUser("user@example.com",
                "password123", Role.USER,
                "Иван",
                "Иванов");

        admin = createUser("admin@example.com",
                "admin123", Role.ADMIN,
                "Админ",
                "Админов");


        // Создание тестового объявления
        testAd = new AdEntity();
        testAd.setTitle("Продам велосипед");
        testAd.setPrice(15000);
        testAd.setDescription("Горный велосипед в отличном состоянии");
        testAd.setAuthor(user);
        testAd = adRepository.save(testAd);

        // Создание объектов Authentication для тестов
        userAuth = TestAuthenticationUtils.createAuthentication("user@example.com", Role.USER);
        adminAuth = TestAuthenticationUtils.createAuthentication("admin@example.com", Role.ADMIN);
        anotherUserAuth = TestAuthenticationUtils.createAuthentication("another@example.com", Role.USER);

        // Создание нескольких тестовых комментариев
        createTestComments();
    }

    /**
     * Создает и сохраняет несколько тестовых комментариев для проверки.
     */
    private void createTestComments() {
        CommentEntity comment1 = new CommentEntity();
        comment1.setText("Отличный велосипед! Хочу купить.");
        comment1.setAuthor(user);
        comment1.setAd(testAd);
        commentRepository.save(comment1);

        CommentEntity comment2 = new CommentEntity();
        comment2.setText("Какой год выпуска?");
        comment2.setAuthor(admin);
        comment2.setAd(testAd);
        commentRepository.save(comment2);
    }

    /**
     * Тест: Получение комментариев существующего объявления.
     * <p>
     * Ожидается:
     * <ul>
     *   <li>Возвращается корректное количество комментариев</li>
     *   <li>Комментарии содержат правильные данные авторов</li>
     *   <li>Порядок комментариев соответствует БД</li>
     * </ul>
     */
    @Test
    void getComments_ShouldReturnComments_WhenAdExists() {

        Integer adId = testAd.getId();

        Comments comments = commentService.getComments(adId);

        assertAll(
                () -> assertThat(comments).isNotNull(),
                () -> {
                    // Безопасная проверка count
                    Assertions.assertNotNull(comments);
                    Integer count = comments.getCount();
                    assertThat(count).isNotNull().isEqualTo(2);
                },
                () -> {
                    // Безопасный доступ к results
                    Assertions.assertNotNull(comments);
                    List<Comment> results = comments.getResults();
                    assertThat(results).isNotNull().hasSize(2);

                    Comment firstComment = results.get(0);
                    Comment secondComment = results.get(1);

                    assertThat(firstComment).isNotNull();
                    assertThat(firstComment.getText()).isNotNull().contains("Отличный велосипед");
                    assertThat(firstComment.getAuthorFirstName()).isNotNull().isEqualTo("Иван");

                    assertThat(secondComment).isNotNull();
                    assertThat(secondComment.getText()).isNotNull().contains("Какой год выпуска");
                    assertThat(secondComment.getAuthorFirstName()).isNotNull().isEqualTo("Админ");
                }
        );
    }

    /**
     * Тест: Получение комментариев несуществующего объявления.
     * <p>
     * Ожидается: {@link AdNotFoundException} с HTTP статусом 404.
     */
    @Test
    void getComments_ShouldThrowException_WhenAdNotFound() {

        Integer nonExistentAdId = 9999;

        assertThatThrownBy(() -> commentService.getComments(nonExistentAdId))
                .isInstanceOf(AdNotFoundException.class)
                .hasMessageContaining("Объявление с ID " + nonExistentAdId + " не найдено");
    }

    /**
     * Тест: Добавление нового комментария к существующему объявлению.
     * <p>
     * Ожидается:
     * <ul>
     *   <li>Комментарий успешно создается</li>
     *   <li>Автор комментария соответствует аутентифицированному пользователю</li>
     *   <li>Поля createdAt и pk заполняются автоматически</li>
     *   <li>Общее количество комментариев увеличивается на 1</li>
     * </ul>
     */
    @Test
    void addComment_ShouldCreateComment_WhenValidData() {

        Integer adId = testAd.getId();
        CreateOrUpdateComment newCommentDto = new CreateOrUpdateComment();
        newCommentDto.setText("Новый комментарий для теста");

        Comment createdComment = commentService.addComment(adId, userAuth, newCommentDto);

        assertAll(
                () -> assertThat(createdComment).isNotNull(),
                () -> {
                    Assertions.assertNotNull(createdComment);
                    assertThat(createdComment.getText()).isEqualTo("Новый комментарий для теста");
                },
                () -> {
                    Assertions.assertNotNull(createdComment);
                    assertThat(createdComment.getAuthor()).isEqualTo(user.getId());
                },
                () -> {
                    Assertions.assertNotNull(createdComment);
                    assertThat(createdComment.getAuthorFirstName()).isEqualTo("Иван");
                },
                () -> {
                    Assertions.assertNotNull(createdComment);
                    assertThat(createdComment.getPk()).isNotNull();
                },
                () -> {
                    Assertions.assertNotNull(createdComment);
                    assertThat(createdComment.getCreatedAt()).isNotNull();
                }
        );

        // Проверяем, что комментарий сохранен в БД
        List<CommentEntity> allComments = commentRepository.findAllByAdId(adId);
        assertThat(allComments).hasSize(3);
    }

    /**
     * Тест: Добавление комментария к несуществующему объявлению.
     * <p>
     * Ожидается: {@link AdNotFoundException} с HTTP статусом 404.
     */
    @Test
    void addComment_ShouldThrowException_WhenAdNotFound() {

        Integer nonExistentAdId = 9999;
        CreateOrUpdateComment newCommentDto = new CreateOrUpdateComment();
        newCommentDto.setText("Комментарий к несуществующему объявлению");

        assertThatThrownBy(() -> commentService.addComment(nonExistentAdId, userAuth, newCommentDto))
                .isInstanceOf(AdNotFoundException.class)
                .hasMessageContaining("Объявление с ID " + nonExistentAdId + " не найдено");
    }

    /**
     * Тест: Удаление своего комментария пользователем USER.
     * <p>
     * Ожидается:
     * <ul>
     *   <li>Комментарий успешно удаляется</li>
     *   <li>Общее количество комментариев уменьшается на 1</li>
     *   <li>Не выбрасываются исключения доступа</li>
     * </ul>
     */
    @Test
    void deleteComment_ShouldDeleteOwnComment_WhenUserIsAuthor() {

        CommentEntity userComment = commentRepository.findAllByAdId(testAd.getId())
                .stream()
                .filter(c -> c.getAuthor().getEmail().equals("user@example.com"))
                .findFirst()
                .orElseThrow();

        Integer commentId = userComment.getId();
        Integer adId = testAd.getId();

        long initialCount = commentRepository.count();


        commentService.deleteComment(adId, commentId, userAuth);

        assertAll(
                () -> assertThat(commentRepository.findById(commentId)).isEmpty(),
                () -> assertThat(commentRepository.count()).isEqualTo(initialCount - 1)
        );
    }

    /**
     * Тест: Удаление чужого комментария администратором ADMIN.
     * <p>
     * Ожидается:
     * <ul>
     *   <li>Комментарий успешно удаляется (ADMIN имеет права на все)</li>
     *   <li>Не выбрасываются исключения доступа</li>
     * </ul>
     */
    @Test
    void deleteComment_ShouldDeleteAnyComment_WhenUserIsAdmin() {

        CommentEntity userComment = commentRepository.findAllByAdId(testAd.getId())
                .stream()
                .filter(c -> c.getAuthor().getEmail().equals("user@example.com"))
                .findFirst()
                .orElseThrow();

        Integer commentId = userComment.getId();
        Integer adId = testAd.getId();

        long initialCount = commentRepository.count();


        commentService.deleteComment(adId, commentId, adminAuth);

        assertAll(
                () -> assertThat(commentRepository.findById(commentId)).isEmpty(),
                () -> assertThat(commentRepository.count()).isEqualTo(initialCount - 1)
        );
    }

    /**
     * Тест: Попытка удаления чужого комментария пользователем USER.
     * <p>
     * Ожидается: {@link AccessDeniedException} с HTTP статусом 403.
     */
    @Test
    void deleteComment_ShouldThrowException_WhenUserTriesToDeleteOtherUsersComment() {

        CommentEntity adminComment = commentRepository.findAllByAdId(testAd.getId())
                .stream()
                .filter(c -> c.getAuthor().getEmail().equals("admin@example.com"))
                .findFirst()
                .orElseThrow();

        Integer commentId = adminComment.getId();
        Integer adId = testAd.getId();

        // другой пользователь пытается удалить комментарий администратора
        assertThatThrownBy(() -> commentService.deleteComment(adId, commentId, anotherUserAuth))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Нет доступа к comment с ID " + commentId);
    }

    /**
     * Тест: Удаление несуществующего комментария.
     * <p>
     * Ожидается: {@link CommentNotFoundException} с HTTP статусом 404.
     */
    @Test
    void deleteComment_ShouldThrowException_WhenCommentNotFound() {

        Integer nonExistentCommentId = 9999;
        Integer adId = testAd.getId();

        assertThatThrownBy(() -> commentService.deleteComment(adId, nonExistentCommentId, userAuth))
                .isInstanceOf(CommentNotFoundException.class)
                .hasMessageContaining("Комментарий с ID " + nonExistentCommentId + " не найден");
    }

    /**
     * Тест: Обновление своего комментария пользователем USER.
     * <p>
     * Ожидается:
     * <ul>
     *   <li>Текст комментария успешно обновляется</li>
     *   <li>Автор и дата создания остаются неизменными</li>
     *   <li>Возвращается обновленный DTO с новым текстом</li>
     * </ul>
     */
    @Test
    void updateComment_ShouldUpdateOwnComment_WhenUserIsAuthor() {

        CommentEntity userComment = commentRepository.findAllByAdId(testAd.getId())
                .stream()
                .filter(c -> c.getAuthor().getEmail().equals("user@example.com"))
                .findFirst()
                .orElseThrow();

        Integer commentId = userComment.getId();
        Integer adId = testAd.getId();
        String originalText = userComment.getText();
        Integer originalAuthorId = userComment.getAuthor().getId();

        CreateOrUpdateComment updateDto = new CreateOrUpdateComment();
        updateDto.setText("Обновленный текст комментария");

        Comment updatedComment = commentService.updateComment(adId, commentId, userAuth, updateDto);

        assertAll(
                () -> assertThat(updatedComment).isNotNull(),
                () -> {
                    Assertions.assertNotNull(updatedComment);
                    assertThat(updatedComment.getText()).isEqualTo("Обновленный текст комментария");
                },
                () -> {
                    Assertions.assertNotNull(updatedComment);
                    assertThat(updatedComment.getText()).isNotEqualTo(originalText);
                },
                () -> {
                    Assertions.assertNotNull(updatedComment);
                    assertThat(updatedComment.getAuthor()).isEqualTo(originalAuthorId);
                },
                () -> {
                    Assertions.assertNotNull(updatedComment);
                    assertThat(updatedComment.getPk()).isEqualTo(commentId);
                }
        );

        // Проверяем обновление в БД
        CommentEntity updatedEntity = commentRepository.findById(commentId).orElseThrow();
        assertThat(updatedEntity.getText()).isEqualTo("Обновленный текст комментария");
    }

    /**
     * Тест: Обновление чужого комментария администратором ADMIN.
     * <p>
     * Ожидается:
     * <ul>
     *   <li>Комментарий успешно обновляется (ADMIN имеет права на все)</li>
     *   <li>Не выбрасываются исключения доступа</li>
     * </ul>
     */
    @Test
    void updateComment_ShouldUpdateAnyComment_WhenUserIsAdmin() {

        CommentEntity userComment = commentRepository.findAllByAdId(testAd.getId())
                .stream()
                .filter(c -> c.getAuthor().getEmail().equals("user@example.com"))
                .findFirst()
                .orElseThrow();

        Integer commentId = userComment.getId();
        Integer adId = testAd.getId();

        CreateOrUpdateComment updateDto = new CreateOrUpdateComment();
        updateDto.setText("Текст обновлен администратором");

        Comment updatedComment = commentService.updateComment(adId, commentId, adminAuth, updateDto);

        assertThat(updatedComment.getText()).isEqualTo("Текст обновлен администратором");
    }

    /**
     * Тест: Попытка обновления чужого комментария пользователем USER.
     * <p>
     * Ожидается: {@link AccessDeniedException} с HTTP статусом 403.
     */
    @Test
    void updateComment_ShouldThrowException_WhenUserTriesToUpdateOtherUsersComment() {

        CommentEntity adminComment = commentRepository.findAllByAdId(testAd.getId())
                .stream()
                .filter(c -> c.getAuthor().getEmail().equals("admin@example.com"))
                .findFirst()
                .orElseThrow();

        Integer commentId = adminComment.getId();
        Integer adId = testAd.getId();

        CreateOrUpdateComment updateDto = new CreateOrUpdateComment();
        updateDto.setText("Попытка изменить чужой комментарий");

        // другой пользователь пытается обновить комментарий администратора
        assertThatThrownBy(() -> commentService.updateComment(adId, commentId, anotherUserAuth, updateDto))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Нет доступа к comment с ID " + commentId);
    }

    /**
     * Тест: Попытка обновления комментария с несоответствующим adId.
     * <p>
     * Ожидается: {@link IllegalArgumentException} с HTTP статусом 400.
     * Ситуация возникает, когда commentId принадлежит другому объявлению.
     */
    @Test
    void updateComment_ShouldThrowException_WhenAdIdDoesNotMatch() {

        // Создаем второе объявление и комментарий к нему
        AdEntity anotherAd = new AdEntity();
        anotherAd.setTitle("Второе объявление");
        anotherAd.setPrice(5000);
        anotherAd.setDescription("Другое объявление");
        anotherAd.setAuthor(user);
        anotherAd = adRepository.save(anotherAd);

        CommentEntity commentForAnotherAd = new CommentEntity();
        commentForAnotherAd.setText("Комментарий для другого объявления");
        commentForAnotherAd.setAuthor(user);
        commentForAnotherAd.setAd(anotherAd);
        commentForAnotherAd = commentRepository.save(commentForAnotherAd);

        // Пытаемся обновить комментарий, указав неверный adId
        Integer wrongAdId = testAd.getId(); // ID первого объявления
        Integer commentId = commentForAnotherAd.getId();

        CreateOrUpdateComment updateDto = new CreateOrUpdateComment();
        updateDto.setText("Новый текст");

        assertThatThrownBy(() -> commentService.updateComment(wrongAdId, commentId, userAuth, updateDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Комментарий не принадлежит указанному объявлению");
    }

    /**
     * Создает и сохраняет тестового пользователя.
     *
     * @param email email пользователя
     * @param password пароль (будет захеширован)
     * @param role роль пользователя
     * @param firstName имя пользователя
     * @param lastName фамилия пользователя
     * @return сохраненная сущность пользователя
     */
    private UserEntity createUser(String email, String password, Role role, String firstName, String lastName) {
        UserEntity userEntity = new UserEntity();
        userEntity.setEmail(email);
        userEntity.setPassword(passwordEncoder.encode(password));
        userEntity.setFirstName(firstName);
        userEntity.setLastName(lastName);
        userEntity.setPhone("+7 (999) 123-45-67");
        userEntity.setRole(role);
        userEntity.setEnabled(true);
        return userRepository.save(userEntity);
    }

}
