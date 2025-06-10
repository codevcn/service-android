package com.example.taskmanager.controller;

import com.example.taskmanager.dev.DevLogger;
import com.example.taskmanager.dto.ApiResponse;
import com.example.taskmanager.dto.ProjectDTO;
import com.example.taskmanager.exception.ResourceConflictException;
import com.example.taskmanager.model.Project;
import com.example.taskmanager.model.ProjectMember;
import com.example.taskmanager.model.User;
import com.example.taskmanager.model.Notification;
import com.example.taskmanager.repository.ProjectRepository;
import com.example.taskmanager.repository.UserRepository;
import com.example.taskmanager.service.NotificationService;
import com.example.taskmanager.repository.ProjectMemberRepository;
import com.example.taskmanager.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class ProjectController {
	@Autowired
	private ProjectRepository projectRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ProjectMemberRepository projectMemberRepository;
	@Autowired
	private NotificationService notificationService;
	@Autowired
	private UserService userService;

	@PostMapping("/projects")
	public ResponseEntity<?> createProject(@Valid @RequestBody Project project,
			@AuthenticationPrincipal UserDetails userDetails) {
		var user = userRepository.findByUsername(userDetails.getUsername())
				.orElseThrow(() -> new EntityNotFoundException("User not found"));

		project.setOwner(user);
		var savedProject = projectRepository.save(project);

		ProjectMember projectMember = new ProjectMember();
		projectMember.setUser(user);
		projectMember.setProject(savedProject);
		projectMember.setRole(ProjectMember.Role.Admin);
		projectMemberRepository.save(projectMember);

		return ResponseEntity.status(HttpStatus.CREATED)
				.body(new ApiResponse("success", ProjectDTO.fromEntity(savedProject), null));
	}

	@GetMapping("/projects")
	public ResponseEntity<?> getProjects(@AuthenticationPrincipal UserDetails userDetails) {
		var user = userRepository.findByEmail(userDetails.getUsername())
				.orElseThrow(() -> new EntityNotFoundException("User not found"));
		List<Project> projects = projectRepository.findByOwnerOrderByCreatedAtDesc(user);
		List<ProjectDTO> dtos = projects.stream().map(ProjectDTO::fromEntity).collect(Collectors.toList());
		return ResponseEntity.ok(new ApiResponse("success", dtos, null));
	}

	@GetMapping("/projects/{projectId}")
	public ResponseEntity<?> getProject(@PathVariable Long projectId,
			@AuthenticationPrincipal UserDetails userDetails) {
		var user = userRepository.findByUsername(userDetails.getUsername())
				.orElseThrow(() -> new EntityNotFoundException("User not found"));

		var project = projectRepository.findById(projectId)
				.orElseThrow(() -> new EntityNotFoundException(
						"Project not found with id: " + projectId));

		var projectMember = projectMemberRepository.findByUser_IdAndProject_Id(user.getId(), projectId);

		if (!project.getOwner().equals(user) && !projectMember.isPresent()) {
			throw new EntityNotFoundException("You are not a member of the project");
		}

		return ResponseEntity.ok(new ApiResponse("success", ProjectDTO.fromEntity(project), null));
	}

	@PutMapping("/projects/{projectId}")
	public ResponseEntity<?> updateProject(@PathVariable Long projectId, @RequestBody Project projectRequest,
			@AuthenticationPrincipal UserDetails userDetails) {
		var user = userRepository.findByUsername(userDetails.getUsername())
				.orElseThrow(() -> new EntityNotFoundException("User not found"));

		var role = userService.getRoleInProject(user.getId(), projectId);
		if (role != ProjectMember.Role.Admin) {
			throw new EntityNotFoundException("User has no permission to update project");
		}

		var project = projectRepository.findById(projectId)
				.orElseThrow(() -> new EntityNotFoundException(
						"Project not found with id: " + projectId));

		if (!project.getOwner().equals(user)) {
			throw new EntityNotFoundException("Project does not belong to the current user");
		}

		// Check if new project name conflicts with existing projects
		if (!project.getProjectName().equals(projectRequest.getProjectName()) &&
				projectRepository.existsByProjectNameAndOwner_Username(projectRequest.getProjectName(),
						user.getUsername())) {
			throw new ResourceConflictException("Project name already exists for this user");
		}

		if (projectRequest.getProjectName() != null && !projectRequest.getProjectName().isEmpty()) {
			project.setProjectName(projectRequest.getProjectName());
		}
		if (projectRequest.getDescription() != null && !projectRequest.getDescription().isEmpty()) {
			project.setDescription(projectRequest.getDescription());
		}
		if (projectRequest.getStatus() != null && !projectRequest.getStatus().isEmpty()) {
			project.setStatus(projectRequest.getStatus());
		}
		if (projectRequest.getStartDate() != null) {
			project.setStartDate(projectRequest.getStartDate());
		}
		if (projectRequest.getEndDate() != null) {
			project.setEndDate(projectRequest.getEndDate());
		}

		var updatedProject = projectRepository.save(project);
		return ResponseEntity.ok(new ApiResponse("success", ProjectDTO.fromEntity(updatedProject), null));
	}

	@DeleteMapping("/projects/{projectId}")
	public ResponseEntity<?> deleteProject(@PathVariable Long projectId,
			@AuthenticationPrincipal UserDetails userDetails) {
		var user = userRepository.findByUsername(userDetails.getUsername())
				.orElseThrow(() -> new EntityNotFoundException("User not found"));

		var project = projectRepository.findById(projectId)
				.orElseThrow(() -> new EntityNotFoundException(
						"Project not found with id: " + projectId));

		if (!project.getOwner().equals(user)) {
			throw new EntityNotFoundException("User is not the owner of the project");
		}

		projectRepository.delete(project);
		return ResponseEntity.ok(new ApiResponse("success", "Project deleted successfully", null));
	}

	@GetMapping("/projects/{projectId}/members")
	public ResponseEntity<?> getProjectMembers(@PathVariable Long projectId) {
		var members = projectMemberRepository.findAll().stream()
				.filter(member -> member.getProject().getId().equals(projectId))
				.collect(Collectors.toList());
		return ResponseEntity.ok(new ApiResponse("success", members, null));
	}

	@DeleteMapping("/projects/{projectId}/leave")
	public ResponseEntity<?> leaveProject(@PathVariable Long projectId,
			@AuthenticationPrincipal UserDetails userDetails) {
		var user = userRepository.findByUsername(userDetails.getUsername())
				.orElseThrow(() -> new EntityNotFoundException("User not found"));

		var projectMember = projectMemberRepository.findByUser_IdAndProject_Id(user.getId(), projectId)
				.orElseThrow(() -> new EntityNotFoundException("You are not a member of this project"));

		// Don't allow project owner to leave
		var project = projectRepository.findById(projectId)
				.orElseThrow(() -> new EntityNotFoundException("Project not found"));
		if (project.getOwner().equals(user)) {
			throw new ResourceConflictException("Project owner cannot leave the project");
		}

		projectMemberRepository.delete(projectMember);
		return ResponseEntity.ok(new ApiResponse("success", "Successfully left the project", null));
	}

	// send project invitation to user
	@PostMapping("/projects/{projectId}/invite")
	public ResponseEntity<?> sendProjectInvitation(@PathVariable Long projectId,
			@RequestBody(required = true) SendProjectInvitationRequest request,
			@AuthenticationPrincipal UserDetails userDetails) {
		var project = projectRepository.findById(projectId)
				.orElseThrow(() -> new EntityNotFoundException("Project not found"));
		var sender = userRepository.findByUsername(userDetails.getUsername())
				.orElseThrow(() -> new EntityNotFoundException("User not found"));
		for (Long userId : request.userIds()) {
			var projectMember = projectMemberRepository.findByUser_IdAndProject_Id(userId, projectId);
			if (projectMember.isPresent()) {
				throw new ResourceConflictException(
						"User " + projectMember.get().getUser().getFullname() + " is already a member of this project");
			}
		}
		for (Long userId : request.userIds()) {
			var invitedUser = userRepository.findById(userId)
					.orElseThrow(() -> new EntityNotFoundException("User not found"));
			// send notification to invited user
			notificationService.notifyProjectInvitation(invitedUser, project, sender);
			notificationService.notifyGeneral(sender, "You have invited " + invitedUser.getFullname() + " to project: "
					+ project.getProjectName());
		}
		return ResponseEntity.ok(new ApiResponse("success", "Project invitation sent successfully", null));
	}

	@PostMapping("/projects/invitations/{notificationId}/accept")
	public ResponseEntity<?> acceptProjectInvitation(@PathVariable Long notificationId,
			@AuthenticationPrincipal UserDetails userDetails) {
		Optional<User> fetchedUser = userRepository.findByEmail(userDetails.getUsername());
		if (fetchedUser.isEmpty()) {
			throw new EntityNotFoundException("User not found");
		}
		Optional<Notification> fetchedNotification = notificationService.getNotificationById(notificationId);
		if (fetchedNotification.isEmpty()) {
			throw new EntityNotFoundException("Notification not found");
		}

		User user = fetchedUser.get();
		User sender = fetchedNotification.get().getSender();
		Notification notification = fetchedNotification.get();
		Project project = notification.getProject();

		ProjectMember projectMember = new ProjectMember();
		projectMember.setUser(user);
		projectMember.setProject(project);
		projectMember.setRole(ProjectMember.Role.Member);
		projectMemberRepository.save(projectMember);

		notificationService.updateAction(notificationId, Notification.Action.ACCEPT);
		notificationService.notifyGeneral(
				sender,
				user.getFullname() + " has accepted your invitation to project: " + project.getProjectName());
		notificationService.broadcastProjectMemberAdded(sender, project);

		return ResponseEntity.ok(new ApiResponse("success", "Project invitation accepted successfully", null));
	}

	@PostMapping("/projects/invitations/{notificationId}/reject")
	public ResponseEntity<?> rejectProjectInvitation(@PathVariable Long notificationId) {
		Optional<Notification> fetchedNotification = notificationService.getNotificationById(notificationId);
		if (fetchedNotification.isEmpty()) {
			throw new EntityNotFoundException("Notification not found");
		}
		Notification notification = fetchedNotification.get();
		notificationService.updateAction(notificationId, Notification.Action.REJECT);
		notificationService.notifyGeneral(
				notification.getSender(),
				notification.getUser().getFullname() + " has rejected your invitation to project: "
						+ notification.getProject().getProjectName());
		return ResponseEntity.ok(new ApiResponse("success", "Project invitation rejected successfully", null));
	}

	@GetMapping("/projects/joined")
	public ResponseEntity<?> getJoinedProjects(@AuthenticationPrincipal UserDetails userDetails) {
		var user = userRepository.findByUsername(userDetails.getUsername())
				.orElseThrow(() -> new EntityNotFoundException("User not found"));
		var projects = projectMemberRepository.findByUser_Id(user.getId()).stream()
				.map(ProjectMember::getProject)
				.collect(Collectors.toList());
		List<ProjectDTO> dtos = projects.stream().map(ProjectDTO::fromEntity).collect(Collectors.toList());
		return ResponseEntity.ok(new ApiResponse("success", dtos, null));
	}

	private record SendProjectInvitationRequest(Long[] userIds) {
	}
}
