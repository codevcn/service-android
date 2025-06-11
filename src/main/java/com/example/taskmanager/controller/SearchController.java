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
		long ownerId = owner.getId();
		List<Project> ownedProjects = projectRepository.findByOwnerId(ownerId);
		List<Project> joinedProjects = projectMemberRepository.findByUser_Id(ownerId).stream()
				.map(ProjectMember::getProject).toList();

		ArrayList<Project> allProjects = new ArrayList<>(ownedProjects);
		for (Project project : joinedProjects) {
			if (!allProjects.stream().anyMatch(p -> p.getId().equals(project.getId()))) {
				allProjects.add(project);
			}
		}

		ArrayList<Project> projectsByKeyword = new ArrayList<>();
		for (Project project : ownedProjects) {
			if (project.getProjectName().toLowerCase().contains(keyword.toLowerCase())) {
				projectsByKeyword.add(project);
			}
		}
		for (Project project : joinedProjects) {
			if (!allProjects.stream().anyMatch(p -> p.getId().equals(project.getId()))
					&& project.getProjectName().toLowerCase().contains(keyword.toLowerCase())) {
				projectsByKeyword.add(project);
			}
		}

		List<Long> projectIdsToSearch = allProjects.stream().map(Project::getId).toList();
		List<Phase> phases = phaseRepository.findByProjectIdInAndNameContainingKeyword(projectIdsToSearch, keyword);
		List<Task> tasks = taskRepository.findByProjectIdsAndTaskNameContainingKeyword(projectIdsToSearch, keyword);

		List<TaskDTO> taskDTOs = tasks.stream().map(TaskDTO::fromEntity).toList();
		List<ProjectDTO> projectDTOs = projectsByKeyword.stream().map(ProjectDTO::fromEntity).toList();
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