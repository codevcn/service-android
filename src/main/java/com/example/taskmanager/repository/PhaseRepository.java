package com.example.taskmanager.repository;

import com.example.taskmanager.model.Phase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PhaseRepository extends JpaRepository<Phase, Long> {
    List<Phase> findByProjectIdOrderByOrderIndexAsc(Long projectId);

    boolean existsByPhaseNameAndProjectId(String phaseName, Long projectId);

    List<Phase> findByPhaseNameContaining(String phaseName);

    @Query("SELECT p FROM Phase p WHERE p.project.id = ?1 AND LOWER(p.phaseName) LIKE LOWER(CONCAT('%', ?2, '%'))")
    List<Phase> findByProjectIdAndNameContainingKeyword(Long projectId, String keyword);

    @Query("SELECT p FROM Phase p WHERE p.project.id IN ?1 AND LOWER(p.phaseName) LIKE LOWER(CONCAT('%', ?2, '%'))")
    List<Phase> findByProjectIdInAndNameContainingKeyword(List<Long> projectIds, String keyword);

    long countByProject_Id(Long projectId);
}