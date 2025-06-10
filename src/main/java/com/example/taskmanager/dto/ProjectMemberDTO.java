package com.example.taskmanager.dto;

import com.example.taskmanager.model.ProjectMember;

public record ProjectMemberDTO(
    Long id,
    Long userId,
    Long projectId,
    ProjectMember.Role role) {

  public static ProjectMemberDTO fromEntity(ProjectMember projectMember) {
    return new ProjectMemberDTO(projectMember.getMemberId(), projectMember.getUser().getId(),
        projectMember.getProject().getId(), projectMember.getRole());
  }
}