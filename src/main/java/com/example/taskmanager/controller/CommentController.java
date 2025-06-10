package com.example.taskmanager.controller;

import com.example.taskmanager.dto.ApiResponse;
import com.example.taskmanager.dto.CommentDTO;
import com.example.taskmanager.dto.CommentRequestDTO;
import com.example.taskmanager.model.Comment;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.CommentRepository;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.repository.UserRepository;
import com.example.taskmanager.repository.ProjectMemberRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

	@Autowired
	private CommentRepository commentRepository;

	@Autowired
	private TaskRepository taskRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ProjectMemberRepository projectMemberRepository;

	@PostMapping("/{taskId}/{userId}")
	public ResponseEntity<ApiResponse> createComment(
			@PathVariable Long taskId,
			@PathVariable Long userId,
			@RequestBody CommentRequestDTO request) {
		Task task = taskRepository.findById(taskId)
				.orElseThrow(() -> new RuntimeException("Task not found"));
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new RuntimeException("User not found"));

		Comment comment = new Comment();
		comment.setContent(request.content());
		comment.setTask(task);
		comment.setUser(user);

		commentRepository.save(comment);

		// Get project role for the user
		String role = projectMemberRepository.findByProject_IdAndUser_Id(
				task.getPhase().getProject().getId(),
				userId)
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
		comment.setContent(request.content());
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
}