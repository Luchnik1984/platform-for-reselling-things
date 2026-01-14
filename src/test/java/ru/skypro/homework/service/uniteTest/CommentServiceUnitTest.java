package ru.skypro.homework.service.uniteTest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import ru.skypro.homework.dto.comments.Comment;
import ru.skypro.homework.dto.comments.CreateOrUpdateComment;
import ru.skypro.homework.entity.AdEntity;
import ru.skypro.homework.entity.CommentEntity;
import ru.skypro.homework.entity.UserEntity;
import ru.skypro.homework.exceptions.AccessDeniedException;
import ru.skypro.homework.exceptions.AdNotFoundException;
import ru.skypro.homework.exceptions.CommentNotFoundException;
import ru.skypro.homework.mapper.CommentMapper;
import ru.skypro.homework.repository.AdRepository;
import ru.skypro.homework.repository.CommentRepository;
import ru.skypro.homework.repository.UserRepository;
import ru.skypro.homework.service.impl.CommentServiceImpl;
import ru.skypro.homework.util.SecurityUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit-тесты {@link CommentServiceImpl}.
 * <p>
 * Фокус: работа с комментариями + проверка прав.
 */
@ExtendWith(MockitoExtension.class)
class CommentServiceUnitTest {

    @Mock private CommentRepository commentRepository;
    @Mock private AdRepository adRepository;
    @Mock private CommentMapper commentMapper;
    @Mock private UserRepository userRepository;
    @Mock private Authentication authentication;

    @InjectMocks
    private CommentServiceImpl commentService;

    @Test
    void updateComment_shouldThrowCommentNotFound_whenMissing() {
        when(commentRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(CommentNotFoundException.class,
                () -> commentService.updateComment(10, 1, authentication, new CreateOrUpdateComment()));
    }

    @Test
    void updateComment_shouldThrowAccessDenied_whenNotAdminAndNotAuthor() {
        when(authentication.getName()).thenReturn("not-author@test.ru");

        UserEntity author = new UserEntity();
        author.setEmail("author@test.ru");

        AdEntity ad = new AdEntity();
        ad.setId(10);

        CommentEntity entity = new CommentEntity();
        entity.setId(1);
        entity.setAuthor(author);
        entity.setAd(ad);

        when(commentRepository.findById(1)).thenReturn(Optional.of(entity));

        CreateOrUpdateComment dto = new CreateOrUpdateComment();
        dto.setText("New text!!");

        try (MockedStatic<SecurityUtils> utils = mockStatic(SecurityUtils.class)) {
            utils.when(() -> SecurityUtils.isAdmin(authentication)).thenReturn(false);

            assertThrows(AccessDeniedException.class,
                    () -> commentService.updateComment(10, 1, authentication, dto));

            verify(commentRepository, never()).save(any());
        }
    }

    @Test
    void updateComment_shouldUpdate_whenAdmin() {
        when(authentication.getName()).thenReturn("admin@test.ru");

        UserEntity author = new UserEntity();
        author.setEmail("author@test.ru");

        AdEntity ad = new AdEntity();
        ad.setId(10);

        CommentEntity entity = new CommentEntity();
        entity.setId(1);
        entity.setAuthor(author);
        entity.setAd(ad);

        when(commentRepository.findById(1)).thenReturn(Optional.of(entity));

        CreateOrUpdateComment dto = new CreateOrUpdateComment();
        dto.setText("New text!!");

        Comment response = new Comment();
        response.setPk(1);
        response.setText("New text!!");

        try (MockedStatic<SecurityUtils> utils = mockStatic(SecurityUtils.class)) {
            utils.when(() -> SecurityUtils.isAdmin(authentication)).thenReturn(true);

            when(commentRepository.save(entity)).thenReturn(entity);
            when(commentMapper.toDto(entity)).thenReturn(response);

            Comment result = commentService.updateComment(10, 1, authentication, dto);

            assertNotNull(result);
            assertEquals("New text!!", result.getText());
            verify(commentMapper).updateEntityFromDto(dto, entity);
            verify(commentRepository).save(entity);
        }
    }

    @Test
    void updateComment_shouldUpdate_whenAuthor() {
        when(authentication.getName()).thenReturn("author@test.ru");

        UserEntity author = new UserEntity();
        author.setEmail("author@test.ru");

        AdEntity ad = new AdEntity();
        ad.setId(10);

        CommentEntity entity = new CommentEntity();
        entity.setId(1);
        entity.setAuthor(author);
        entity.setAd(ad);

        when(commentRepository.findById(1)).thenReturn(Optional.of(entity));

        CreateOrUpdateComment dto = new CreateOrUpdateComment();
        dto.setText("New text!!");

        Comment response = new Comment();
        response.setPk(1);
        response.setText("New text!!");

        try (MockedStatic<SecurityUtils> utils = mockStatic(SecurityUtils.class)) {
            utils.when(() -> SecurityUtils.isAdmin(authentication)).thenReturn(false);

            when(commentRepository.save(entity)).thenReturn(entity);
            when(commentMapper.toDto(entity)).thenReturn(response);

            Comment result = commentService.updateComment(10, 1, authentication, dto);

            assertNotNull(result);
            assertEquals("New text!!", result.getText());
            verify(commentMapper).updateEntityFromDto(dto, entity);
            verify(commentRepository).save(entity);
        }
    }

    @Test
    void getComments_shouldThrowAdNotFoundException_whenAdMissing() {
        when(adRepository.existsById(10)).thenReturn(false);

        assertThrows(AdNotFoundException.class, () -> commentService.getComments(10));

        verify(commentRepository, never()).findAllByAdId(anyInt());
        verifyNoInteractions(commentMapper);

    }

    @Test
    void addComment_shouldThrowAdNotFoundException_whenAdMissing() {
        when(authentication.getName()).thenReturn("user@test.ru");

        CreateOrUpdateComment dto = new CreateOrUpdateComment();
        dto.setText("Hello!");

        try (MockedStatic<SecurityUtils> utils = mockStatic(SecurityUtils.class)) {
            utils.when(() -> SecurityUtils.getAuthenticatedUser(userRepository, authentication))
                    .thenReturn(new UserEntity());

            when(adRepository.findById(10)).thenReturn(Optional.empty());

            assertThrows(AdNotFoundException.class,
                    () -> commentService.addComment(10, authentication, dto));

            verify(commentRepository, never()).save(any());
        }
    }

    @Test
    void deleteComment_shouldThrowIllegalArgument_whenCommentFromAnotherAd() {
        when(authentication.getName()).thenReturn("author@test.ru");

        AdEntity realAd = new AdEntity();
        realAd.setId(999);

        UserEntity author = new UserEntity();
        author.setEmail("author@test.ru");

        CommentEntity comment = new CommentEntity();
        comment.setId(1);
        comment.setAd(realAd);
        comment.setAuthor(author);

        when(commentRepository.findById(1)).thenReturn(Optional.of(comment));

        assertThrows(IllegalArgumentException.class,
                () -> commentService.deleteComment(10, 1, authentication));

        verify(commentRepository, never()).delete(any());
        verifyNoInteractions(commentMapper);
    }

    @Test
    void deleteComment_shouldDelete_whenAuthor() {
        when(authentication.getName()).thenReturn("author@test.ru");

        AdEntity ad = new AdEntity();
        ad.setId(10);

        UserEntity author = new UserEntity();
        author.setEmail("author@test.ru");

        CommentEntity comment = new CommentEntity();
        comment.setId(1);
        comment.setAd(ad);
        comment.setAuthor(author);

        when(commentRepository.findById(1)).thenReturn(Optional.of(comment));

        try (MockedStatic<SecurityUtils> utils = mockStatic(SecurityUtils.class)) {
            utils.when(() -> SecurityUtils.isAdmin(authentication)).thenReturn(false);

            commentService.deleteComment(10, 1, authentication);

            verify(commentRepository).delete(comment);
        }
    }

    @Test
    void deleteComment_shouldDelete_whenAdmin() {
        when(authentication.getName()).thenReturn("admin@test.ru");

        AdEntity ad = new AdEntity();
        ad.setId(10);

        UserEntity author = new UserEntity();
        author.setEmail("author@test.ru");

        CommentEntity comment = new CommentEntity();
        comment.setId(1);
        comment.setAd(ad);
        comment.setAuthor(author);

        when(commentRepository.findById(1)).thenReturn(Optional.of(comment));

        try (MockedStatic<SecurityUtils> utils = mockStatic(SecurityUtils.class)) {
            utils.when(() -> SecurityUtils.isAdmin(authentication)).thenReturn(true);

            commentService.deleteComment(10, 1, authentication);

            verify(commentRepository).delete(comment);
        }
    }
}