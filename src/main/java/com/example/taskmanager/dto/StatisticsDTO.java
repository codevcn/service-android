package com.example.taskmanager.dto;

import java.util.List;

public record StatisticsDTO(
		// Project statistics
		long ownedProjectsCount,
		long joinedProjectsCount,
		long totalProjectsCount,

		// Task statistics
		long totalTasksCount,
		long completedTasksCount,
		long pendingTasksCount,

		// Project member statistics
		List<ProjectMemberStatisticsDTO> projectMemberStats,

		// Phase statistics
		List<PhaseStatisticsDTO> phaseStats) {
}