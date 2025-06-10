package com.example.taskmanager.controller.ping;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.taskmanager.dev.DevLogger;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/ping")
public class PingController {
	@GetMapping
	public ResponseEntity<PingResponse> ping() {
		try {
			DevLogger.logToFile("PingController.ping: " + "pong 2222");
		} catch (Exception e) {
			DevLogger.logToFile("PingController.ping: " + e.getMessage());
			return ResponseEntity.internalServerError().body(new PingResponse("error: " + e.getMessage()));
		}
		return ResponseEntity.ok(new PingResponse("pong 2222"));
	}

	private record PingResponse(String pingContent) {
	}
}
