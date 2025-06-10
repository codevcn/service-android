package com.example.taskmanager.dev;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DevLogger {
	public static void logToFile(String message) {
		try {
			// Store log file in project root directory instead of classpath
			String logPath = "logs/dev.log";
			try (PrintWriter writer = new PrintWriter(new FileWriter(logPath, true))) {
				LocalDateTime now = LocalDateTime.now();
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
				String timestamp = now.format(formatter);
				writer.println(">>> " + timestamp + " - " + message);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
