package com.example.taskmanager.controller;

import com.example.taskmanager.dto.ApiResponse;
import com.example.taskmanager.dto.CommentDTO;
import com.example.taskmanager.dto.CommentRequestDTO;
import com.example.taskmanager.model.Comment;
import com.example.taskmanager.model.ProjectMember;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.CommentRepository;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.repository.UserRepository;
import com.example.taskmanager.service.NotificationService;
import com.example.taskmanager.repository.ProjectMemberRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.taskmanager.dev.DevLogger;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/comments")
@Tag(name = "Comments", description = "Comment management APIs")
public class CommentController {

	@Autowired
	private CommentRepository commentRepository;

	@Autowired
	private TaskRepository taskRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ProjectMemberRepository projectMemberRepository;

	@Autowired
	private NotificationService notificationService;

	@PostMapping("/{taskId}")
	public ResponseEntity<ApiResponse> createComment(
			@PathVariable Long taskId,
			@RequestBody CommentRequestDTO request,
			@AuthenticationPrincipal UserDetails userDetails) {
				DevLogger.logToFile("Creating comment for task ID: " + taskId + " by user: " + userDetails.getUsername());
		Task task = taskRepository.findById(taskId)
				.orElseThrow(() -> new RuntimeException("Task not found"));
		User user = userRepository.findByEmail(userDetails.getUsername())
				.orElseThrow(() -> new RuntimeException("User not found"));

		Comment comment = new Comment();
		comment.setContent(request.content());
		comment.setTask(task);
		comment.setUser(user);
		comment.setIsTaskResult(false);

		commentRepository.save(comment);

		List<ProjectMember> projectMembers = projectMemberRepository
				.findByProject_IdAndRoleIn(task.getPhase().getProject().getId(),
						List.of(ProjectMember.Role.Admin, ProjectMember.Role.Leader));
		for (ProjectMember projectMember : projectMembers) {
			notificationService.notifyGeneral(projectMember.getUser(), "Task updated: " + task.getTaskName());
		}
		User assignedTo = task.getAssignedTo();
		if (assignedTo != null) {
			notificationService.notifyGeneral(assignedTo, "Task updated: " + task.getTaskName());
		}

		// Get project role for the user
		String role = projectMemberRepository.findByProject_IdAndUser_Id(
				task.getPhase().getProject().getId(),
				user.getId())
				.map(member -> member.getRole().toString())
				.orElse("GUEST");

		return ResponseEntity.ok(new ApiResponse("success", CommentDTO.fromEntity(comment, role), null));
	}

	// update a comment
	@PutMapping("/{commentId}")
	public ResponseEntity<?> updateComment(@PathVariable Long commentId,
			@RequestBody CommentRequestDTO request) {
		Comment comment = commentRepository.findById(commentId)
				.orElseThrow(() -> new RuntimeException("Comment not found"));
		if (request.isTaskResult()) {
			comment.setIsTaskResult(request.isTaskResult());
		}
		if (request.content() != null) {
			comment.setContent(request.content());
		}
		commentRepository.save(comment);
		return ResponseEntity.ok(new ApiResponse("success", "Comment updated successfully", null));
	}

	@GetMapping("/task/{taskId}")
	public ResponseEntity<?> getCommentsByTaskId(@PathVariable Long taskId) {
		// Get task and verify it exists
		Optional<Task> task = taskRepository.findById(taskId);
		if (!task.isPresent()) {
			throw new EntityNotFoundException("Task not found");
		}

		// Get all comments for the task with user data pre-fetched
		List<Comment> comments = commentRepository.findByTaskIdWithUser(taskId);

		// Get project ID from task
		Long projectId = task.get().getPhase().getProject().getId();

		// Convert comments to DTOs with roles
		List<CommentDTO> commentDTOs = comments.stream()
				.map(comment -> {
					String role = projectMemberRepository
							.findByProject_IdAndUser_Id(projectId,
									comment.getUser().getId())
							.map(member -> member.getRole().toString())
							.orElse("GUEST");
					return CommentDTO.fromEntity(comment, role);
				})
				.collect(Collectors.toList());

		return ResponseEntity.ok(new ApiResponse("success", commentDTOs, null));
	}

	@DeleteMapping("/{commentId}")
	public ResponseEntity<ApiResponse> deleteComment(@PathVariable Long commentId) {
		if (commentRepository.existsById(commentId)) {
			commentRepository.deleteById(commentId);
			return ResponseEntity.ok(new ApiResponse("success", "Comment deleted successfully", null));
		}
		return ResponseEntity.badRequest()
				.body(new ApiResponse("error", "Comment not found", null));
	}

	// mark comment as task result
	@PutMapping("/{commentId}/mark-as-task-result")
	public ResponseEntity<?> markCommentAsTaskResult(@PathVariable Long commentId,
			@AuthenticationPrincipal UserDetails userDetails) {
		Comment comment = commentRepository.findById(commentId)
				.orElseThrow(() -> new RuntimeException("Comment not found"));
		comment.setIsTaskResult(true);
		commentRepository.save(comment);
		return ResponseEntity.ok(new ApiResponse("success", "Comment marked as task result", null));
	}
}