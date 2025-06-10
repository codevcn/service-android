package com.example.taskmanager.dto;

public record PhaseStatisticsDTO(
    Long projectId,
    String projectName,
    long phaseCount) {
}
