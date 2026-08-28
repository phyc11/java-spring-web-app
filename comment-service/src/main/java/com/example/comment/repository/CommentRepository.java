package com.example.comment.repository;

import com.example.comment.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByTaskIdOrderByCreatedAtAsc(Long taskId);
    List<Comment> findByTaskIdAndParentIdIsNullOrderByCreatedAtAsc(Long taskId);
    List<Comment> findByParentIdOrderByCreatedAtAsc(Long parentId);
    long countByTaskId(Long taskId);
}
