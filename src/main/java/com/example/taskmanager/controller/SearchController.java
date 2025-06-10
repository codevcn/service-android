package com.example.taskmanager.controller;

import com.example.taskmanager.dev.DevLogger;
import com.example.taskmanager.dto.ApiResponse;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.Phase;
import com.example.taskmanager.model.Project;
import com.example.taskmanager.model.ProjectMember;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.repository.ProjectRepository;
import com.example.taskmanager.repository.PhaseRepository;
import com.example.taskmanager.repository.ProjectMemberRepository;
import com.example.taskmanager.repository.UserRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/search")
@Tag(name = "Search", description = "Search APIs")
public class SearchController {
	@Autowired
	private TaskRepository taskRepository;
	@Autowired
	private ProjectRepository projectRepository;
	@Autowired
	private PhaseRepository phaseRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ProjectMemberRepository projectMemberRepository;

	@GetMapping("/general")
	public ResponseEntity<?> searchGenerally(@RequestParam String keyword,
			@AuthenticationPrincipal UserDetails userDetails) {
		User owner = userRepository.findByEmail(userDetails.getUsername())
				.orElseThrow(() -> new EntityNotFoundException("User not found"));
		List<Project> projects = projectRepository.findByOwnerId(owner.getId());
		ArrayList<Project> filteredProjects = new ArrayList<>();
		for (Project project : projects) {
			if (project.getProjectName().toLowerCase().contains(keyword.toLowerCase())) {
				filteredProjects.add(project);
			}
		}
		List<ProjectMember> projectMembers = projectMemberRepository
				.findByUser_IdAndProject_ProjectNameContaining(owner.getId(), keyword);
		for (ProjectMember projectMember : projectMembers) {
			Project relatedProject = projectMember.getProject();
			boolean projectExists = projects.stream()
					.anyMatch(p -> p.getId().equals(relatedProject.getId()));
			if (!projectExists) {
				filteredProjects.add(relatedProject);
			}
		}
		List<Long> projectIds = filteredProjects.stream().map(Project::getId).toList();
		List<Phase> phases = phaseRepository.findByProjectIdInAndNameContainingKeyword(projectIds, keyword);
		List<Task> tasks = taskRepository.findByProjectIdsAndTaskNameContainingKeyword(projectIds, keyword);

		List<TaskDTO> taskDTOs = tasks.stream().map(TaskDTO::fromEntity).toList();
		List<ProjectDTO> projectDTOs = filteredProjects.stream().map(ProjectDTO::fromEntity).toList();
		List<PhaseDTO> phaseDTOs = phases.stream().map(PhaseDTO::fromEntity).toList();

		SearchDTO searchDTO = new SearchDTO(taskDTOs, projectDTOs, phaseDTOs);
		return ResponseEntity.ok(new ApiResponse("success", searchDTO, null));
	}

	private record ProjectDTO(Long id, String title) {
		public static ProjectDTO fromEntity(Project project) {
			return new ProjectDTO(project.getId(), project.getProjectName());
		}
	}

	private record PhaseDTO(Long id, String title, ProjectDTO project) {
		public static PhaseDTO fromEntity(Phase phase) {
			return new PhaseDTO(phase.getId(), phase.getPhaseName(), ProjectDTO.fromEntity(phase.getProject()));
		}
	}

	private record TaskDTO(Long id, String title, PhaseDTO phase, ProjectDTO project) {
		public static TaskDTO fromEntity(Task task) {
			return new TaskDTO(task.getId(), task.getTaskName(), PhaseDTO.fromEntity(task.getPhase()),
					ProjectDTO.fromEntity(task.getPhase().getProject()));
		}
	}

	private record SearchDTO(List<TaskDTO> tasks, List<ProjectDTO> projects, List<PhaseDTO> phases) {
	}
}