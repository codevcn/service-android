package com.example.taskmanager.dto;

import com.example.taskmanager.model.Task;
import java.time.LocalDateTime;

public record TaskCommentDTO(
    Long id,
    String content,
    Long taskId,
    Long userId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static TaskCommentDTO fromEntity(Task task) {
        return new TaskCommentDTO(
            task.getId(),
            task.getDescription(),
            task.getId(),
            task.getAssignedTo() != null ? task.getAssignedTo().getId() : null,
            task.getCreatedAt(),
            task.getUpdatedAt()
        );
    }
} 