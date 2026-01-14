package ru.skypro.homework.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.skypro.homework.dto.comments.Comment;
import ru.skypro.homework.dto.comments.CreateOrUpdateComment;
import ru.skypro.homework.entity.AdEntity;
import ru.skypro.homework.entity.CommentEntity;
import ru.skypro.homework.entity.ImageEntity;
import ru.skypro.homework.entity.UserEntity;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("CommentMapper impl tests (relations)")
class CommentMapperTest {

    private final CommentMapper mapper = Mappers.getMapper(CommentMapper.class);

    @Test
    @DisplayName("toDto маппит связи: comment.author + author.image")
    void toDto_mapsRelations_authorAndImage() {
        ImageEntity image = new ImageEntity();
        image.setFilePath("/uploads/avatars/user-1-avatar.jpg");
        image.setFileSize(1L);
        image.setMediaType("image/jpeg");

        UserEntity author = new UserEntity();
        author.setId(1);
        author.setFirstName("Иван");
        author.setImage(image);

        AdEntity ad = new AdEntity();
        ad.setId(10);

        CommentEntity entity = new CommentEntity();
        entity.setId(5);
        entity.setText("Текст");
        entity.setAuthor(author);
        entity.setAd(ad);
        entity.setCreatedAt(LocalDateTime.of(2024, 1, 15, 10, 30));

        Comment dto = mapper.toDto(entity);

        assertEquals(5, dto.getPk());
        assertEquals(1, dto.getAuthor());
        assertEquals("Иван", dto.getAuthorFirstName());
        // Проверка глубокой связи: author.image.getImageUrl() = "/images" + filePath
        assertEquals(image.getImageUrl(), dto.getAuthorImage());
    }

    @Test
    @DisplayName("toDto конвертирует createdAt в epoch millis")
    void toDto_convertsCreatedAtToMillis() {
        CommentEntity entity = new CommentEntity();
        entity.setId(1);
        entity.setText("Текст");
        entity.setCreatedAt(LocalDateTime.of(2024, 1, 15, 10, 30));

        Comment dto = mapper.toDto(entity);

        long expected = LocalDateTime.of(2024, 1, 15, 10, 30)
                .toInstant(ZoneOffset.UTC)
                .toEpochMilli();

        assertEquals(expected, dto.getCreatedAt());
    }

    @Test
    @DisplayName("toDto null-safety: author=null -> author поля null")
    void toDto_nullSafety_authorNull() {
        CommentEntity entity = new CommentEntity();
        entity.setId(1);
        entity.setText("Текст");
        entity.setAuthor(null);

        Comment dto = mapper.toDto(entity);

        assertNull(dto.getAuthor());
        assertNull(dto.getAuthorFirstName());
        assertNull(dto.getAuthorImage());
    }

    @Test
    @DisplayName("toDto null-safety: author.image=null -> authorFirstName есть, authorImage null")
    void toDto_nullSafety_imageNull() {
        UserEntity author = new UserEntity();
        author.setId(1);
        author.setFirstName("Иван");
        author.setImage(null);

        CommentEntity entity = new CommentEntity();
        entity.setId(1);
        entity.setText("Текст");
        entity.setAuthor(author);

        Comment dto = mapper.toDto(entity);

        assertEquals(1, dto.getAuthor());
        assertEquals("Иван", dto.getAuthorFirstName());
        assertNull(dto.getAuthorImage());
    }

    @Test
    @DisplayName("toEntity маппит только text, связи игнорируются")
    void toEntity_mapsOnlyText() {
        CreateOrUpdateComment dto = new CreateOrUpdateComment();
        dto.setText("Новый");

        CommentEntity entity = mapper.toEntity(dto);

        assertEquals("Новый", entity.getText());
        assertNull(entity.getAuthor());
        assertNull(entity.getAd());
    }
}