package com.example.taskmanager.dto;

import com.example.taskmanager.model.Phase;
import java.time.LocalDateTime;

public record PhaseDTO(
    Long id,
    String phaseName,
    String description,
    String status,
    LocalDateTime startDate,
    LocalDateTime endDate,
    Long projectId,
    Integer orderIndex,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static PhaseDTO fromEntity(Phase phase) {
        return new PhaseDTO(
            phase.getId(),
            phase.getPhaseName(),
            phase.getDescription(),
            phase.getStatus(),
            phase.getStartDate(),
            phase.getEndDate(),
            phase.getProject() != null ? phase.getProject().getId() : null,
            phase.getOrderIndex(),
            phase.getCreatedAt(),
            phase.getUpdatedAt()
        );
    }
} 