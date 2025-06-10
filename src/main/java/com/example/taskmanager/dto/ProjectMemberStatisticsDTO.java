package com.example.taskmanager.dto;

public record ProjectMemberStatisticsDTO(
    Long projectId,
    String projectName,
    long memberCount
) {} 