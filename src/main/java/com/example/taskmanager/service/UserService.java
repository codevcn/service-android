package com.example.taskmanager.service;

import com.example.taskmanager.dto.UserDTO;
import com.example.taskmanager.dto.auth.SignupRequest;
import com.example.taskmanager.model.ProjectMember;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.UserRepository;
import com.example.taskmanager.repository.ProjectMemberRepository;
import jakarta.persistence.EntityNotFoundException;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ProjectMemberRepository projectMemberRepository;

    @Transactional
    public UserDTO createUser(SignupRequest signupRequest) {
        // Validate username
        if (signupRequest.getUsername() == null || signupRequest.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }

        // Validate email
        if (signupRequest.getEmail() == null || signupRequest.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }

        // Validate password
        if (signupRequest.getPassword() == null || signupRequest.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        if (signupRequest.getPassword().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }

        // Validate fullname
        if (signupRequest.getFullname() == null || signupRequest.getFullname().trim().isEmpty()) {
            throw new IllegalArgumentException("Full name cannot be empty");
        }

        User user = new User();
        user.setFullname(signupRequest.getFullname());
        user.setUsername(signupRequest.getEmail());
        user.setEmail(signupRequest.getEmail());
        user.setEmailVerified(true);
        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setLastUpdated(LocalDateTime.now());

        return UserDTO.fromEntity(userRepository.save(user));
    }

    public ProjectMember.Role getRoleInProject(Long userId, Long projectId) {
        return projectMemberRepository.findByUser_IdAndProject_Id(userId, projectId)
                .map(ProjectMember::getRole)
                .orElseThrow(() -> new EntityNotFoundException("User not found in project with id: " + projectId));
    }

    public UserDTO getUserById(Long id) {
        return userRepository.findById(id)
                .map(UserDTO::fromEntity)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
    }

    public UserDTO getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(UserDTO::fromEntity)
                .orElseThrow(() -> new EntityNotFoundException("User not found with username: " + username));
    }

    public UserDTO getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(UserDTO::fromEntity)
                .orElseThrow(() -> new EntityNotFoundException("User not found with email: " + email));
    }

    public String encryptPassword(String password) {
        return passwordEncoder.encode(password);
    }
}