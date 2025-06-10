package com.example.taskmanager.controller;

import com.example.taskmanager.dto.UserDTO;
import com.example.taskmanager.dev.DevLogger;
import com.example.taskmanager.dto.ApiResponse;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.UserRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User management APIs")
public class UserController {
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@GetMapping("/me")
	public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
		var user = userRepository.findByEmail(userDetails.getUsername())
				.orElseThrow(() -> new EntityNotFoundException("User not found"));
		return ResponseEntity.ok(new ApiResponse("success", UserDTO.fromEntity(user), null));
	}

	@PutMapping("/update/profile")
	public ResponseEntity<?> updateUserProfile(
			@RequestBody(required = false) UserProfileRequest request,
			@AuthenticationPrincipal UserDetails userDetails) throws IOException {

		var currentUser = userRepository.findByEmail(userDetails.getUsername())
				.orElseThrow(() -> new EntityNotFoundException("User not found"));

		String fullname = request.fullname();
		String avatar = request.avatar();
		String birthday = request.birthday();
		String bio = request.bio();
		String socialLinks = request.socialLinks();
		String gender = request.gender();

		// Update fullname if provided
		if (fullname != null && !fullname.trim().isEmpty()) {
			currentUser.setFullname(fullname.trim());
		}
		// Update avatar if provided
		if (avatar != null && !avatar.trim().isEmpty()) {
			currentUser.setAvatar(avatar.trim());
		}
		// Update birthday if provided
		if (birthday != null && !birthday.trim().isEmpty()) {
			currentUser.setBirthday(LocalDate.parse(birthday.trim()));
		}
		// Update bio if provided
		if (bio != null && !bio.trim().isEmpty()) {
			currentUser.setBio(bio.trim());
		}
		// Update social links if provided
		if (socialLinks != null && !socialLinks.trim().isEmpty()) {
			currentUser.setSocialLinks(socialLinks.trim());
		}
		// Update gender if provided
		if (gender != null && !gender.trim().isEmpty()) {
			currentUser.setGender(User.Gender.valueOf(gender.trim()));
		}

		var updatedUser = userRepository.save(currentUser);
		return ResponseEntity.ok(new ApiResponse("success", UserDTO.fromEntity(updatedUser), null));
	}

	@PutMapping("/update/password")
	public ResponseEntity<?> updateUserPassword(
			@Valid @RequestBody PasswordUpdateRequest request,
			@AuthenticationPrincipal UserDetails userDetails) {

		var currentUser = userRepository.findByEmail(userDetails.getUsername())
				.orElseThrow(() -> new EntityNotFoundException("User not found"));

		// Verify old password
		if (!passwordEncoder.matches(request.oldPassword(), currentUser.getPassword())) {
			throw new IllegalArgumentException("Current password is incorrect");
		}

		// Validate new password
		if (request.newPassword().length() < 6) {
			throw new IllegalArgumentException("New password must be at least 6 characters");
		}

		// Update password
		currentUser.setPassword(passwordEncoder.encode(request.newPassword()));
		userRepository.save(currentUser);

		return ResponseEntity.ok(new ApiResponse("success", "Password updated successfully", null));
	}

	@GetMapping("/search")
	public ResponseEntity<?> searchUsers(
			@RequestParam String query,
			@AuthenticationPrincipal UserDetails userDetails) {
		var currentUser = userRepository.findByEmail(userDetails.getUsername())
				.orElseThrow(() -> new EntityNotFoundException("User not found"));

		List<User> users = userRepository.findByUsernameContainingOrFullnameContaining(query, query);
		// Filter out current user from results
		users = users.stream()
				.filter(user -> !user.getId().equals(currentUser.getId()))
				.collect(Collectors.toList());

		List<UserDTO> dtos = users.stream()
				.map(UserDTO::fromEntity)
				.collect(Collectors.toList());

		return ResponseEntity.ok(new ApiResponse("success", dtos, null));
	}

	@GetMapping("/{userId}")
	public ResponseEntity<?> getUserById(@PathVariable Long userId) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new EntityNotFoundException("User not found"));
		return ResponseEntity.ok(new ApiResponse("success", UserDTO.fromEntity(user), null));
	}

	// Request DTOs
	public record PasswordUpdateRequest(
			@NotBlank(message = "Current password is required") String oldPassword,
			@NotBlank(message = "New password is required") String newPassword) {
	}

	public record UserProfileRequest(
			String fullname,
			String avatar,
			String birthday,
			String bio,
			String socialLinks,
			String gender) {
	}
}