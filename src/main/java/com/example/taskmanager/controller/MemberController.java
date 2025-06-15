package com.example.taskmanager.controller;

import com.example.taskmanager.dev.DevLogger;
import com.example.taskmanager.dto.ApiResponse;
import com.example.taskmanager.dto.ProjectMemberDTO;
import com.example.taskmanager.model.Phase;
import com.example.taskmanager.model.ProjectMember;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.PhaseRepository;
import com.example.taskmanager.repository.ProjectMemberRepository;
import com.example.taskmanager.repository.ProjectRepository;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.repository.UserRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/members")
@Tag(name = "Project Members", description = "Project member management APIs")
public class MemberController {
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ProjectMemberRepository projectMemberRepository;
	@Autowired
	private TaskRepository taskRepository;
	@Autowired
	private PhaseRepository phaseRepository;
	@Autowired
	private ProjectRepository projectRepository;

	@GetMapping("/projects/{projectId}")
	public ResponseEntity<?> getProjectMembers(@PathVariable Long projectId,
			@AuthenticationPrincipal UserDetails userDetails) {
		DevLogger.logToFile("call api get project members with projectId: " + projectId + " by user: " + userDetails.getUsername());
		var user = userRepository.findByEmail(userDetails.getUsername())
				.orElseThrow(() -> new EntityNotFoundException("User not found"));

		// Check if user is a member of the project
		if (!projectMemberRepository.existsByUser_IdAndProject_Id(user.getId(), projectId)) {
			throw new EntityNotFoundException("User is not a member of this project");
		}

		var members = projectMemberRepository.findAll().stream()
				.filter(member -> member.getProject().getId().equals(projectId))
				.map(ProjectMemberDTO::fromEntity)
				.collect(Collectors.toList());
		return ResponseEntity.ok(new ApiResponse("success", members, null));
	}

	@GetMapping("/{userId}/projects/{projectId}")
	public ResponseEntity<?> getProjectMember(@PathVariable Long projectId, @PathVariable Long userId) {
		// Get the requested member's information
		ProjectMember projectMember = projectMemberRepository.findByUser_IdAndProject_Id(userId, projectId)
				.orElseThrow(() -> new EntityNotFoundException("Member not found in this project"));

		return ResponseEntity.ok(new ApiResponse("success", ProjectMemberDTO.fromEntity(projectMember), null));
	}

	// add a member to a task
	@PostMapping("/add-member")
	public ResponseEntity<?> addMemberToTask(@RequestParam Long taskId, @RequestParam Long userId,
			@RequestParam Long projectId) {
		// check if task is already assigned to a user
		Task task = taskRepository.findById(taskId)
				.orElseThrow(() -> new EntityNotFoundException("Task not found"));
		if (task.getAssignedTo() != null) {
			throw new IllegalArgumentException("Task is already assigned to a user");
		}
		// // find all phase ids of the project
		// List<Long> phaseIds = phaseRepository.findByProjectIdOrderByOrderIndexAsc(projectId).stream()
		// 		.map(Phase::getId)
		// 		.collect(Collectors.toList());
		// Check if user is already assigned to a task in this project
		// Task existingTask = taskRepository.findByAssignedToIdAndPhaseIdIn(userId, phaseIds);
		// if (existingTask != null) {
		// 	throw new IllegalArgumentException("User is already assigned to a task in this project");
		// }
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new EntityNotFoundException("User not found"));
		task.setAssignedTo(user);
		taskRepository.save(task);
		return ResponseEntity.ok(new ApiResponse("success", "Member added to task successfully", null));
	}

	// remove a member from a task
	@DeleteMapping("/remove-member")
	public ResponseEntity<?> removeMemberFromTask(@RequestParam Long taskId, @RequestParam Long userId) {
		Task task = taskRepository.findById(taskId)
				.orElseThrow(() -> new EntityNotFoundException("Task not found"));
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new EntityNotFoundException("User not found"));
		if (!task.getAssignedTo().equals(user)) {
			throw new EntityNotFoundException("User is not assigned to this task");
		}
		task.setAssignedTo(null);
		taskRepository.save(task);
		return ResponseEntity.ok(new ApiResponse("success", "Member removed from task successfully", null));
	}

	// remove a member from a project
	@DeleteMapping("/remove-project-member")
	public ResponseEntity<?> removeMemberFromProject(@RequestParam Long projectId, @RequestParam Long userId,
			@AuthenticationPrincipal UserDetails userDetails) {
		var user = userRepository.findByEmail(userDetails.getUsername())
				.orElseThrow(() -> new EntityNotFoundException("User not found"));
		// check if user is the owner of the project
		if (projectRepository.findByOwnerId(user.getId()).isEmpty()) {
			throw new EntityNotFoundException("User is not the owner of this project");
		}
		ProjectMember projectMember = projectMemberRepository.findByUser_IdAndProject_Id(userId, projectId)
				.orElseThrow(() -> new EntityNotFoundException("Member not found in this project"));
		projectMemberRepository.delete(projectMember);
		return ResponseEntity.ok(new ApiResponse("success", "Member removed from project successfully", null));
	}

	// change role of a member in a project
	@PutMapping("/change-role")
	public ResponseEntity<?> changeRole(@RequestParam Long projectId, @RequestParam Long userId,
			@RequestParam String role, @AuthenticationPrincipal UserDetails userDetails) {
		var user = userRepository.findByEmail(userDetails.getUsername())
				.orElseThrow(() -> new EntityNotFoundException("User not found"));
		// check if user is the owner of the project
		if (projectRepository.findByOwnerId(user.getId()).isEmpty()) {
			throw new EntityNotFoundException("User is not the owner of this project");
		}
		ProjectMember projectMember = projectMemberRepository.findByUser_IdAndProject_Id(userId, projectId)
				.orElseThrow(() -> new EntityNotFoundException("Member not found in this project"));
		ProjectMember.Role newRole;
		try {
			newRole = ProjectMember.Role.valueOf(role);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Invalid role");
		}
		projectMember.setRole(newRole);
		projectMemberRepository.save(projectMember);
		return ResponseEntity.ok(new ApiResponse("success", "Role changed successfully", null));
	}
}
