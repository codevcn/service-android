package com.example.taskmanager.repository;

import com.example.taskmanager.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByTask_IdOrderByCreatedAtDesc(Long taskId);

    @Query("SELECT c FROM Comment c JOIN FETCH c.user WHERE c.task.id = :taskId ORDER BY c.createdAt DESC")
    List<Comment> findByTaskIdWithUser(@Param("taskId") Long taskId);
} 