package com.example.taskmanager.dto;

import com.example.taskmanager.model.Notification;
import com.example.taskmanager.model.Project;
import com.example.taskmanager.model.User;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificationDTO {
    private Long notificationId;
    private Long userId;
    private String message;
    private boolean read;
    private LocalDateTime createdAt;
    private Notification.Type type;
    private Notification.Action action;
    private Long projectId;
    private Long senderId;

    public static NotificationDTO fromEntity(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        Project project = notification.getProject();
        User sender = notification.getSender();
        dto.setProjectId(project != null ? project.getId() : null);
        dto.setNotificationId(notification.getNotificationId());
        dto.setUserId(notification.getUser().getId());
        dto.setMessage(notification.getMessage());
        dto.setRead(notification.isRead());
        dto.setCreatedAt(notification.getCreatedAt());
        dto.setType(notification.getType());
        dto.setAction(notification.getAction());
        dto.setSenderId(sender != null ? sender.getId() : null);
        return dto;
    }
}