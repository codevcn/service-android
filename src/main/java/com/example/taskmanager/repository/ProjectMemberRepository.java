package com.example.taskmanager.repository;

import com.example.taskmanager.model.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {
    List<ProjectMember> findByProject_Id(Long projectId);

    Optional<ProjectMember> findByProject_IdAndUser_Id(Long projectId, Long userId);

    boolean existsByUser_IdAndProject_Id(Long userId, Long projectId);

    List<ProjectMember> findByUser_IdInAndProject_Id(List<Long> userIds, Long projectId);

    Optional<ProjectMember> findByUser_IdAndProject_Id(Long userId, Long projectId);

    List<ProjectMember> findByUser_Id(Long userId);

    List<ProjectMember> findByUser_IdAndProject_ProjectNameContaining(Long userId, String keyword);

    long countByUser_Id(Long userId);

    long countByProject_Id(Long projectId);

    void deleteByUser_IdAndProject_Id(Long userId, Long projectId);
}