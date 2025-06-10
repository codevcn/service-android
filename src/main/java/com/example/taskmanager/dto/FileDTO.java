package com.example.taskmanager.dto;

import com.example.taskmanager.model.File;
import java.time.LocalDateTime;

public record FileDTO(
    Long id,
    String fileName,
    String filePath,
    String fileType,
    Long fileSize,
    Long taskId,
    Long userId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static FileDTO fromEntity(File file) {
        return new FileDTO(
            file.getId(),
            file.getFileName(),
            file.getFilePath(),
            file.getFileType(),
            file.getFileSize(),
            file.getTask().getId(),
            file.getUser().getId(),
            file.getCreatedAt(),
            file.getUpdatedAt()
        );
    }
} 