package com.example.taskmanager.controller;

import com.example.taskmanager.dto.ApiResponse;
import com.example.taskmanager.dto.StatisticsDTO;
import com.example.taskmanager.dto.PhaseStatisticsDTO;
import com.example.taskmanager.dto.ProjectMemberStatisticsDTO;
import com.example.taskmanager.model.User;
import com.example.taskmanager.model.Project;
import com.example.taskmanager.repository.ProjectRepository;
import com.example.taskmanager.repository.ProjectMemberRepository;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.repository.UserRepository;
import com.example.taskmanager.repository.PhaseRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private ProjectRepository projectRepository;

  @Autowired
  private ProjectMemberRepository projectMemberRepository;

  @Autowired
  private TaskRepository taskRepository;

  @Autowired
  private PhaseRepository phaseRepository;

  @GetMapping()
  public ResponseEntity<?> getDashboardStatistics(@AuthenticationPrincipal UserDetails userDetails) {
    User user = userRepository.findByUsername(userDetails.getUsername())
        .orElseThrow(() -> new EntityNotFoundException("User not found"));

    // Project statistics
    long ownedProjectsCount = projectRepository.countByOwner(user);
    long joinedProjectsCount = projectMemberRepository.countByUser_Id(user.getId());
    long totalProjectsCount = ownedProjectsCount + joinedProjectsCount;

    List<Project> projects = projectRepository.findByOwnerId(user.getId());
    List<Long> projectIds = projects.stream()
        .map(Project::getId)
        .collect(Collectors.toList());
    // Task statistics
    long totalTasksCount = taskRepository.countByProjectIdsIn(projectIds);
    long completedTasksCount = taskRepository.countByProjectIdsInAndStatus(projectIds, "DONE");
    long pendingTasksCount = taskRepository.countByProjectIdsInAndStatusIN_PROGRESS(projectIds);

    // Lấy danh sách dự án user sở hữu
    List<Project> ownedProjects = projectRepository.findByOwnerOrderByCreatedAtDesc(user);

    // Thống kê số lượng thành viên cho mỗi dự án
    List<ProjectMemberStatisticsDTO> projectMemberStats = ownedProjects.stream()
        .map(project -> {
          long memberCount = projectMemberRepository.countByProject_Id(project.getId());
          return new ProjectMemberStatisticsDTO(
              project.getId(),
              project.getProjectName(),
              memberCount);
        })
        .collect(Collectors.toList());

    // Thống kê số lượng phase cho mỗi dự án
    List<PhaseStatisticsDTO> phaseStats = ownedProjects.stream()
        .map(project -> {
          long phaseCount = phaseRepository.countByProject_Id(project.getId());
          return new PhaseStatisticsDTO(
              project.getId(),
              project.getProjectName(),
              phaseCount);
        })
        .collect(Collectors.toList());

    StatisticsDTO finalStatistics = new StatisticsDTO(
        ownedProjectsCount,
        joinedProjectsCount,
        totalProjectsCount,
        totalTasksCount,
        completedTasksCount,
        pendingTasksCount,
        projectMemberStats,
        phaseStats);

    return ResponseEntity.ok(new ApiResponse("success", finalStatistics, null));
  }
}