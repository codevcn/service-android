package com.example.taskmanager.dto;

import com.example.taskmanager.model.Project;
import java.time.LocalDateTime;

public record ProjectDTO(
    Long id,
    String projectName,
    String description,
    String status,
    LocalDateTime startDate,
    LocalDateTime endDate,
    Long ownerId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static ProjectDTO fromEntity(Project project) {
        return new ProjectDTO(
            project.getId(),
            project.getProjectName(),
            project.getDescription(),
            project.getStatus(),
            project.getStartDate(),
            project.getEndDate(),
            project.getOwner() != null ? project.getOwner().getId() : null,
            project.getCreatedAt(),
            project.getUpdatedAt()
        );
    }
} 