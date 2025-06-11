package com.example.taskmanager.dto;

import com.example.taskmanager.model.Comment;
import java.time.LocalDateTime;

public record CommentDTO(
		Long id,
		String content,
		Long taskId,
		Long userId,
		String username,
		LocalDateTime createdAt,
		LocalDateTime updatedAt,
		String userRole,
		boolean isTaskResult) {
	public static CommentDTO fromEntity(Comment comment, String role) {
		return new CommentDTO(
				comment.getId(),
				comment.getContent(),
				comment.getTask().getId(),
				comment.getUser().getId(),
				comment.getUser().getUsername(),
				comment.getCreatedAt(),
				comment.getUpdatedAt(),
				role,
				comment.isTaskResult());
	}
}