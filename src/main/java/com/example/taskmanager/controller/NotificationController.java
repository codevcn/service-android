package com.example.taskmanager.controller;

import com.example.taskmanager.dto.ApiResponse;
import com.example.taskmanager.dto.NotificationDTO;
import com.example.taskmanager.model.Notification;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.NotificationRepository;
import com.example.taskmanager.repository.UserRepository;
import com.example.taskmanager.service.NotificationService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

	@Autowired
	private NotificationRepository notificationRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private NotificationService notificationService;

	@GetMapping("/stream")
	public SseEmitter streamEvents(@AuthenticationPrincipal UserDetails userDetails) {
		Optional<User> user = userRepository.findByEmail(userDetails.getUsername());
		if (!user.isPresent()) {
			throw new EntityNotFoundException("User not found");
		}
		return notificationService.registerEmitter(user.get().getId());
	}

	@GetMapping()
	public ResponseEntity<?> getNotifications(@AuthenticationPrincipal UserDetails userDetails) {
		var user = userRepository.findByUsername(userDetails.getUsername())
				.orElseThrow(() -> new EntityNotFoundException("User not found"));

		List<Notification> notifications = notificationRepository.findByUserOrderByCreatedAtDesc(user);
		List<NotificationDTO> dtos = notifications.stream().map(NotificationDTO::fromEntity)
				.collect(Collectors.toList());
		return ResponseEntity.ok(new ApiResponse("success", dtos, null));
	}

	@PutMapping("/{notificationId}/read")
	public ResponseEntity<?> markAsRead(@PathVariable Long notificationId,
			@AuthenticationPrincipal UserDetails userDetails) {
		var user = userRepository.findByUsername(userDetails.getUsername())
				.orElseThrow(() -> new EntityNotFoundException("User not found"));

		var notification = notificationRepository.findById(notificationId)
				.orElseThrow(() -> new EntityNotFoundException(
						"Notification not found with id: " + notificationId));

		if (!notification.getUser().equals(user)) {
			throw new EntityNotFoundException("Notification does not belong to the current user");
		}

		notification.setRead(true);
		var updatedNotification = notificationRepository.save(notification);
		return ResponseEntity
				.ok(new ApiResponse("success", NotificationDTO.fromEntity(updatedNotification), null));
	}

	@DeleteMapping("/{notificationId}")
	public ResponseEntity<?> deleteNotification(@PathVariable Long notificationId,
			@AuthenticationPrincipal UserDetails userDetails) {
		var user = userRepository.findByUsername(userDetails.getUsername())
				.orElseThrow(() -> new EntityNotFoundException("User not found"));

		var notification = notificationRepository.findById(notificationId)
				.orElseThrow(() -> new EntityNotFoundException(
						"Notification not found with id: " + notificationId));

		if (!notification.getUser().equals(user)) {
			throw new EntityNotFoundException("Notification does not belong to the current user");
		}

		notificationRepository.delete(notification);
		return ResponseEntity.ok(new ApiResponse("success", "Notification deleted successfully", null));
	}

}