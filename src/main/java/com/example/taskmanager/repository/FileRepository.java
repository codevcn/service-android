package com.example.taskmanager.repository;

import com.example.taskmanager.model.File;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FileRepository extends JpaRepository<File, Long> {
    List<File> findByTask_IdOrderByCreatedAtDesc(Long taskId);
    boolean existsByFileNameAndTask_Id(String fileName, Long taskId);
} 