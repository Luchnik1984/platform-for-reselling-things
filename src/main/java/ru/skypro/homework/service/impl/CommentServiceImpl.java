package ru.skypro.homework.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import ru.skypro.homework.dto.comments.Comment;
import ru.skypro.homework.dto.comments.Comments;
import ru.skypro.homework.dto.comments.CreateOrUpdateComment;
import ru.skypro.homework.entity.AdEntity;
import ru.skypro.homework.entity.CommentEntity;
import ru.skypro.homework.entity.UserEntity;
import ru.skypro.homework.mapper.CommentMapper;
import ru.skypro.homework.repository.AdRepository;
import ru.skypro.homework.repository.CommentRepository;
import ru.skypro.homework.repository.UserRepository;
import ru.skypro.homework.service.CommentService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final AdRepository adRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;

    @Override
    @Transactional(readOnly = true)
    public Comments getComments(Integer adId) {
        // Проверка существования объявления
        adRepository.findById(adId).orElseThrow();

        List<Comment> results = commentRepository.findAllByAdId(adId)
                .stream()
                .map(commentMapper::toDto)
                .toList();

        return new Comments(results.size(), results);
    }

    @Override
    public Comment addComment(Integer adId, Integer authorId, CreateOrUpdateComment dto) {
        AdEntity ad = adRepository.findById(adId).orElseThrow();
        UserEntity author = userRepository.findById(authorId).orElseThrow();

        CommentEntity entity = commentMapper.toEntity(dto);
        entity.setAd(ad);
        entity.setAuthor(author);

        return commentMapper.toDto(commentRepository.save(entity));
    }

    @Override
    public void deleteComment(Integer adId, Integer commentId, Integer authorId) {
        // 1) права: комментарий должен принадлежать автору
        CommentEntity entity = commentRepository.findByIdAndAuthorId(commentId, authorId)
                .orElseThrow(() -> new SecurityException("Forbidden"));

        // 2) корректность: коммент должен быть от этого объявления
        if (entity.getAd() == null || entity.getAd().getId() == null || !adId.equals(entity.getAd().getId())) {
            throw new IllegalArgumentException("Comment does not belong to this ad");
        }

        commentRepository.delete(entity);
    }

    @Override
    public Comment updateComment(Integer adId, Integer commentId, Integer authorId, CreateOrUpdateComment dto) {
        CommentEntity entity = commentRepository.findByIdAndAuthorId(commentId, authorId)
                .orElseThrow(() -> new SecurityException("Forbidden"));

        if (entity.getAd() == null || entity.getAd().getId() == null || !adId.equals(entity.getAd().getId())) {
            throw new IllegalArgumentException("Comment does not belong to this ad");
        }

        commentMapper.updateEntityFromDto(dto, entity);
        return commentMapper.toDto(commentRepository.save(entity));
    }
}
