package com.example.taskmanager.dto;

import com.example.taskmanager.model.Task;
import java.time.LocalDateTime;

public record TaskDTO(
    Long id,
    String taskName,
    String description,
    Long phaseId,
    Long assignedToId,
    String status,
    String priority,
    LocalDateTime dueDate,
    boolean allowSelfAssign,
    Integer orderIndex,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static TaskDTO fromEntity(Task task) {
        return new TaskDTO(
            task.getId(),
            task.getTaskName(),
            task.getDescription(),
            task.getPhase() != null ? task.getPhase().getId() : null,
            task.getAssignedTo() != null ? task.getAssignedTo().getId() : null,
            task.getStatus(),
            task.getPriority(),
            task.getDueDate(),
            task.isAllowSelfAssign(),
            task.getOrderIndex(),
            task.getCreatedAt(),
            task.getUpdatedAt()
        );
    }
} 