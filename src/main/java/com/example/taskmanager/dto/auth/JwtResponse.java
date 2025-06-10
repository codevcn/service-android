package com.example.taskmanager.dto.auth;

import com.example.taskmanager.dto.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private UserDTO user;

    public JwtResponse(String token, UserDTO user) {
        this.token = token;
        this.user = user;
    }
} 