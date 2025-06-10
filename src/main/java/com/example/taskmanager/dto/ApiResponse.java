package com.example.taskmanager.dto;

public record ApiResponse(String status, Object data, String error) {
}