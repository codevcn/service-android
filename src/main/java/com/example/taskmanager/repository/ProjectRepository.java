package com.example.taskmanager.repository;

import com.example.taskmanager.model.Project;
import com.example.taskmanager.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    @Query("SELECT p FROM Project p JOIN ProjectMember pm ON p.id = pm.project.id WHERE pm.user.id = ?1")
    List<Project> findAllByUserId(Long userId);

    List<Project> findByOwnerId(Long userId);
    boolean existsByProjectName(String projectName);
    boolean existsByProjectNameAndOwner_Username(String projectName, String username);
    List<Project> findByOwnerOrderByCreatedAtDesc(User owner);
    List<Project> findByProjectNameContaining(String projectName);

    @Query("SELECT p FROM Project p WHERE p.owner = ?1 AND LOWER(p.projectName) LIKE LOWER(CONCAT('%', ?2, '%'))")
    List<Project> findByOwnerAndProjectNameContainingKeyword(User owner, String keyword);

    long countByOwner(User owner);
} 