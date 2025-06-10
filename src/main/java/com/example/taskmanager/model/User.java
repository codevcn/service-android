package com.example.taskmanager.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private String fullname;
    private LocalDate birthday;

    @Enumerated(EnumType.STRING)
    private Gender gender = Gender.Other;

    @Column(columnDefinition = "TEXT")
    private String socialLinks;

    private String avatar = "img/avatar/default.png";

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime lastUpdated;

    private boolean emailVerified = false;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastUpdated = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }

    public enum Gender {
        Male, Female, Other
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", fullname='" + fullname + '\'' +
                ", birthday=" + birthday +
                ", gender=" + gender +
                ", socialLinks='" + socialLinks + '\'' +
                ", avatar='" + avatar + '\'' +
                ", bio='" + bio + '\'' +
                ", createdAt=" + createdAt +
                ", lastUpdated=" + lastUpdated +
                ", emailVerified=" + emailVerified +
                '}';
    }

}