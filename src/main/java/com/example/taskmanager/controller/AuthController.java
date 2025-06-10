package com.example.taskmanager.controller;

import com.example.taskmanager.dev.DevLogger;
import com.example.taskmanager.dto.ApiResponse;
import com.example.taskmanager.dto.UserDTO;
import com.example.taskmanager.dto.auth.LoginRequest;
import com.example.taskmanager.dto.auth.SignupRequest;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.UserRepository;
import com.example.taskmanager.service.JwtService;
import com.example.taskmanager.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private UserService userService;

	@Autowired
	private JwtService jwtService;

	@Autowired
	private UserRepository userRepository;

	@PostMapping("/signup")
	public ResponseEntity<?> registerUser(@RequestBody(required = true) SignupRequest signupRequest) {
		if (userRepository.findByUsernameOrEmail(signupRequest.getUsername(), signupRequest.getEmail()).isPresent()) {
			throw new IllegalArgumentException("Email already exists");
		}
		UserDTO userDTO = userService.createUser(signupRequest);
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(
						signupRequest.getEmail(),
						signupRequest.getPassword()));
		SecurityContextHolder.getContext().setAuthentication(authentication);
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();

		String jwt = jwtService.generateToken(userDetails);

		// Create cookie with JWT token
		ResponseCookie jwtCookie = jwtService.generateJwtCookie(jwt);
		return ResponseEntity.status(HttpStatus.CREATED)
				.header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
				.body(new ApiResponse("success", userDTO, null));
	}

	@PostMapping("/login")
	public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
		DevLogger.logToFile("den server");
		try {
			
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(
						loginRequest.getUsernameOrEmail(),
						loginRequest.getPassword()));
		SecurityContextHolder.getContext().setAuthentication(authentication);
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		User user = userRepository.findByUsername(userDetails.getUsername())
				.orElseGet(() -> userRepository.findByEmail(userDetails.getUsername()).orElse(null));
		String jwt = jwtService.generateToken(userDetails);

		// Create cookie with JWT token
		ResponseCookie jwtCookie = jwtService.generateJwtCookie(jwt);
		
		return ResponseEntity
				.ok()
				.header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
				.body(new ApiResponse("success", UserDTO.fromEntity(user), null));
		} catch (Exception e) {
			// TODO: handle exception
			DevLogger.logToFile("login: "+e.getMessage());
			return ResponseEntity.badRequest().body(new ApiResponse("error",null,e.getMessage()));
		}
	}

	@PostMapping("/logout")
	public ResponseEntity<?> logoutUser() {
		ResponseCookie jwtCookie = jwtService.getCleanJwtCookie();
		return ResponseEntity.ok()
				.header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
				.body(new ApiResponse("success", "Logged out successfully", null));
	}

}