package com.example.taskmanager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
@EnableAsync
public class AsyncConfig {

	@Bean
	public Executor taskExecutor() {
		return Executors.newCachedThreadPool(); // hoặc dùng ThreadPoolTaskExecutor nếu muốn kiểm soát tốt hơn
	}
}
