package com.example.taskmanager.controller.ping;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.taskmanager.dev.DevLogger;
import org.springframework.http.ResponseEntity;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/ping")
public class PingController {
	private static enum EventNames {
		PROJECT_INVITATION,
		TASK_ASSIGNED,
		TASK_UPDATED,
		COMMENT_ADDED
	}

	@GetMapping
	public ResponseEntity<PingResponse> ping() {
		DevLogger.logToFile(EventNames.PROJECT_INVITATION.name());
		ArrayList<String> list = new ArrayList<>();
		String str = list.get(2);
		if (str == null) {
			DevLogger.logToFile("str is null");
		} else {
			DevLogger.logToFile("str is not null");
		}
		return ResponseEntity.ok(new PingResponse("pong 2222"));
	}

	private record PingResponse(String content) {
	}
}
