package com.example.taskmanager.controller;

import com.example.taskmanager.dev.DevLogger;
import com.example.taskmanager.dto.ApiResponse;
import com.example.taskmanager.dto.NotificationDTO;
import com.example.taskmanager.model.Notification;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.NotificationRepository;
import com.example.taskmanager.repository.UserRepository;
import com.example.taskmanager.service.NotificationService;
import com.example.taskmanager.service.BackgroundNotificationSerivice;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
@Tag(name = "Notifications", description = "Notification management APIs")
public class NotificationController {
	private final int NOTIFICATION_LIMIT = 30;

	@Autowired
	private NotificationRepository notificationRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private NotificationService notificationService;
	@Autowired
	private BackgroundNotificationSerivice backgroundNotificationSerivice;

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
		var user = userRepository.findByEmail(userDetails.getUsername())
				.orElseThrow(() -> new EntityNotFoundException("User not found"));

		List<Notification> notifications = notificationRepository.findByUserOrderByCreatedAtDesc(user);
		List<NotificationDTO> dtos = notifications.stream().map(NotificationDTO::fromEntity)
				.collect(Collectors.toList());
		return ResponseEntity.ok(new ApiResponse("success", dtos, null));
	}

	@GetMapping("/with-limit")
	public ResponseEntity<?> getNotificationsWithLimit(@AuthenticationPrincipal UserDetails userDetails) {
		var user = userRepository.findByEmail(userDetails.getUsername())
				.orElseThrow(() -> new EntityNotFoundException("User not found"));

		Pageable pageable = PageRequest.of(0, NOTIFICATION_LIMIT);
		List<Notification> notifications = notificationRepository.findByUserOrderByCreatedAtDescWithLimit(user.getId(), pageable);
		List<NotificationDTO> dtos = notifications.stream().map(NotificationDTO::fromEntity)
				.collect(Collectors.toList());
		return ResponseEntity.ok(new ApiResponse("success", dtos, null));
	}

	@PutMapping("/{notificationId}/read")
	public ResponseEntity<?> markAsRead(@PathVariable Long notificationId,
			@AuthenticationPrincipal UserDetails userDetails) {
		var user = userRepository.findByEmail(userDetails.getUsername())
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
		var user = userRepository.findByEmail(userDetails.getUsername())
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

	@PostMapping("/test-notify")
	public ResponseEntity<?> testNotify(@RequestParam Long userId) {
		var user = userRepository.findById(userId)
				.orElseThrow(() -> new EntityNotFoundException("User not found"));
		notificationService.notifyGeneral(user, "This is a test notification");
		return ResponseEntity.ok(new ApiResponse("success", "Notification sent successfully", null));
	}

	// count unread notifications
	@GetMapping("/count-unread")
	public ResponseEntity<?> countUnreadNotifications(@AuthenticationPrincipal UserDetails userDetails) {
		var user = userRepository.findByEmail(userDetails.getUsername())
				.orElseThrow(() -> new EntityNotFoundException("User not found"));
		var count = notificationRepository.countByUserAndIsRead(user, false);
		return ResponseEntity.ok(new ApiResponse("success", new CountUnreadNotificationsDTO(count), null));
	}

	// mark all notifications as read
	@PutMapping("/mark-all-as-read")
	public ResponseEntity<?> markAllAsRead(@AuthenticationPrincipal UserDetails userDetails) {
		var user = userRepository.findByEmail(userDetails.getUsername())
				.orElseThrow(() -> new EntityNotFoundException("User not found"));
		notificationService.markAllAsRead(user.getId());
		return ResponseEntity.ok(new ApiResponse("success", "All notifications marked as read", null));
	}

	// serve background service
	@GetMapping("/background-service")
	public ResponseEntity<?> backgroundService(@AuthenticationPrincipal UserDetails userDetails) {
		var user = userRepository.findByEmail(userDetails.getUsername())
				.orElseThrow(() -> new EntityNotFoundException("User not found"));
		List<String> messages = backgroundNotificationSerivice.popMessagesByUserId(user.getId());
		if (messages.isEmpty()) {
			return ResponseEntity.ok(new BackgroundServiceResponse(null, "no-message"));
		}
		return ResponseEntity.ok(new BackgroundServiceResponse(messages.get(messages.size() - 1), "success"));
	}

	private record BackgroundServiceResponse(String message, String status) {
	}

	private record CountUnreadNotificationsDTO(Long count) {
	}

}