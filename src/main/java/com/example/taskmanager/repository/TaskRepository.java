package com.example.taskmanager.repository;

import com.example.taskmanager.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByPhaseIdOrderByOrderIndexAsc(Long phaseId);

    List<Task> findByAssignedToId(Long userId);

    boolean existsByTaskNameAndPhaseId(String taskName, Long phaseId);

    List<Task> findByTaskNameContaining(String taskName);

    @Query("SELECT t FROM Task t WHERE t.phase.id IN :phaseIds")
    List<Task> findByPhaseIdIn(@Param("phaseIds") List<Long> phaseIds);

    @Query("SELECT t FROM Task t WHERE t.phase.id = ?1 AND LOWER(t.taskName) LIKE LOWER(CONCAT('%', ?2, '%'))")
    List<Task> findByPhaseIdAndTitleContainingKeyword(Long phaseId, String keyword);

    @Query("SELECT t FROM Task t WHERE t.assignedTo.id = :userId AND t.phase.id IN :phaseIds")
    Task findByAssignedToIdAndPhaseIdIn(@Param("userId") Long userId, @Param("phaseIds") List<Long> phaseIds);

    @Query("SELECT t FROM Task t WHERE t.phase.project.id IN :projectIds AND LOWER(t.taskName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Task> findByProjectIdsAndTaskNameContainingKeyword(@Param("projectIds") List<Long> projectIds,
            @Param("keyword") String keyword);

    List<Task> findByPhase_IdOrderByCreatedAtDesc(Long phaseId);

    List<Task> findByAssignedTo_IdOrderByCreatedAtDesc(Long assigneeId);

    long countByAssignedTo_Id(Long assigneeId);

    long countByAssignedTo_IdAndStatus(Long assigneeId, String status);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.phase.project.id IN :projectIds AND t.status = :status")
    long countByProjectIdsInAndStatus(@Param("projectIds") List<Long> projectIds, @Param("status") String status);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.phase.project.id IN :projectIds AND t.status IS NULL")
    long countByProjectIdsInAndStatusIN_PROGRESS(@Param("projectIds") List<Long> projectIds);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.phase.project.id IN :projectIds")
    long countByProjectIdsIn(@Param("projectIds") List<Long> projectIds);
}