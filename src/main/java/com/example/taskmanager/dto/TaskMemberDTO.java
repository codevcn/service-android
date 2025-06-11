package com.example.taskmanager.dto;

import java.time.LocalDate;

import com.example.taskmanager.model.ProjectMember;
import com.example.taskmanager.model.User;

import lombok.Data;

@Data
public class TaskMemberDTO {
  private Long id;
  private String username;
  private String email;
  private String fullname;
  private LocalDate birthday;
  private User.Gender gender;
  private String socialLinks;
  private String avatar;
  private String bio;
  private boolean emailVerified;
  private ProjectMember.Role projectRole;
  private User.Role role;

  public static TaskMemberDTO fromEntity(User user, ProjectMember projectMember) {
    TaskMemberDTO dto = new TaskMemberDTO();
    dto.setId(user.getId());
    dto.setUsername(user.getUsername());
    dto.setEmail(user.getEmail());
    dto.setFullname(user.getFullname());
    dto.setBirthday(user.getBirthday());
    dto.setGender(user.getGender());
    dto.setSocialLinks(user.getSocialLinks());
    dto.setAvatar(user.getAvatar());
    dto.setBio(user.getBio());
    dto.setEmailVerified(user.isEmailVerified());
    dto.setProjectRole(projectMember.getRole());
    dto.setRole(User.Role.User);
    return dto;
  }
}
