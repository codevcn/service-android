package com.example.taskmanager.dto;

import com.example.taskmanager.model.User;
import lombok.Data;
import java.time.LocalDate;

@Data
public class UserDTO {
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

    public static UserDTO fromEntity(User user) {
        UserDTO dto = new UserDTO();
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
        return dto;
    }
} 