package ru.skypro.homework.service;

import ru.skypro.homework.dto.comments.Comment;
import ru.skypro.homework.dto.comments.Comments;
import ru.skypro.homework.dto.comments.CreateOrUpdateComment;


public interface CommentService {

    Comments getComments(Integer adId);

    Comment addComment(Integer adId, Integer authorId, CreateOrUpdateComment dto);

    void deleteComment(Integer adId, Integer commentId, Integer authorId);

    Comment updateComment(Integer adId, Integer commentId, Integer authorId, CreateOrUpdateComment dto);
}
