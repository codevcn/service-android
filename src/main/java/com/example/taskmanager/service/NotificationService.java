package com.example.taskmanager.service;

import com.example.taskmanager.dev.DevLogger;
import com.example.taskmanager.dto.NotificationDTO;
import com.example.taskmanager.model.Notification;
import com.example.taskmanager.model.Project;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.NotificationRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimerTask;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class NotificationService {
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    private static enum EventNames {
        GENERAL,
        PROJECT_MEMBER_ADDED,
        TOAST,
        TASK_REMINDER,
        PROJECT_REMINDER,
        STREAM_ESTABLISHED
    }

    public enum ToastType {
        success,
        error,
        warning,
        info
    }

    @Autowired
    private NotificationRepository notificationRepository;

    public SseEmitter registerEmitter(Long userId) {
        SseEmitter emitter = new SseEmitter(0L); // 0L nghĩa là không timeout

        emitters.put(userId, emitter);

        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(() -> emitters.remove(userId));
        emitter.onError((e) -> emitters.remove(userId));

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    emitter.send(SseEmitter.event().name(EventNames.STREAM_ESTABLISHED.name())
                            .data(""));
                } catch (IOException e) {
                    emitter.completeWithError(e);
                }
            }
        }, 3000);

        return emitter;
    }

    @Transactional
    public Notification createNotification(User user, String message, Notification.Type type,
            Notification.Action action, Project project, User sender) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage(message);
        notification.setType(type);
        notification.setAction(action);
        notification.setRead(false);
        notification.setProject(project);
        notification.setSender(sender);
        notificationRepository.save(notification);
        return notification;
    }

    public List<Notification> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Notification> getUnreadUserNotifications(Long userId) {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
    }

    public Optional<Notification> getNotificationById(Long notificationId) {
        return notificationRepository.findById(notificationId);
    }

    public void updateAction(Long notificationId, Notification.Action action) {
        Optional<Notification> notification = getNotificationById(notificationId);
        if (notification.isPresent()) {
            Notification notificationEntity = notification.get();
            notificationEntity.setAction(action);
            notificationRepository.save(notificationEntity);
        }
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadByUserId(userId);
    }

    // Observer pattern implementation
    public void notifyTaskAssigned(User user, String taskName) {
        createNotification(user, "You have been assigned to task: " + taskName, Notification.Type.NORMAL,
                null, null, null);
    }

    public void notifyTaskUpdated(User user, String taskName) {
        createNotification(user, "Task has been updated: " + taskName, Notification.Type.NORMAL,
                null, null, null);
    }

    public void notifyCommentAdded(User user, String taskName) {
        createNotification(user, "New comment added to task: " + taskName, Notification.Type.NORMAL,
                null, null, null);
    }

    public void notifyProjectInvitation(User user, Project project, User sender) {
        String message = "You have been invited to project: " + project.getProjectName();
        Notification notification = createNotification(user, message, Notification.Type.PROJECT_INVITATION,
                Notification.Action.PENDING, project, sender);
        SseEmitter emitter = emitters.get(user.getId());
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name(EventNames.GENERAL.name())
                        .data(NotificationDTO.fromEntity(notification)));
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
        }
    }

    public void notifyGeneral(User user, String message) {
        Notification notification = createNotification(user, message, Notification.Type.NORMAL,
                null, null, null);
        SseEmitter emitter = emitters.get(user.getId());
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().name(EventNames.GENERAL.name())
                        .data(NotificationDTO.fromEntity(notification)));
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
        }
    }

    public void broadcastProjectMemberAdded(User user, Project project) {
        SseEmitter emitter = emitters.get(user.getId());
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().name(EventNames.PROJECT_MEMBER_ADDED.name()).data(""));
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
        }
    }

    public void broadcastToast(User user, String message, ToastType type) {
        SseEmitter emitter = emitters.get(user.getId());
        if (emitter != null) {
            try {
                emitter.send(
                        SseEmitter.event().name(EventNames.TOAST.name())
                                .data(new BroadcastToast(message, type)));
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
        }
    }

    public void notifyTaskReminder(User user, Task task, int reminderTime, Project project) {
        Notification notification = createNotification(user,
                "You have a task due in " + reminderTime + " minutes: " + task.getTaskName() + ", in project: "
                        + project.getProjectName(),
                Notification.Type.TASK_REMINDER, null, project, null);
        SseEmitter emitter = emitters.get(user.getId());
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().name(EventNames.TASK_REMINDER.name())
                        .data(NotificationDTO.fromEntity(notification)));
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
        }
    }

    public void notifyProjectReminder(User user, Project project, int reminderTime) {
        Notification notification = createNotification(user,
                "You have a project due in " + reminderTime + " minutes: " + project.getProjectName(),
                Notification.Type.TASK_REMINDER, null, project, null);
        SseEmitter emitter = emitters.get(user.getId());
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().name(EventNames.PROJECT_REMINDER.name())
                        .data(NotificationDTO.fromEntity(notification)));
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
        }
    }

    private record BroadcastToast(String message, ToastType type) {
    }
}
