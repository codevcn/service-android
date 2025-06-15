package com.example.taskmanager.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.persistence.EntityNotFoundException;

import com.example.taskmanager.dev.DevLogger;
import com.example.taskmanager.model.Project;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.TaskRepository;

@Component
public class TaskReminderManager {
  private final Map<Long, Timer> timers = new ConcurrentHashMap<>();
  private final int REMINDER_TIME_THRESHOLD = 10;

  @Autowired
  private NotificationService notificationService;

  @Autowired
  private TaskRepository taskRepository;

  public void scheduleReminder(Task task) {
    long taskId = task.getId();

    // Huỷ nếu đã tồn tại
    if (timers.containsKey(taskId)) {
      cancelReminder(taskId);
    }

    LocalDateTime now = LocalDateTime.now();
    LocalDateTime reminderTime = task.getDueDate().minusMinutes(REMINDER_TIME_THRESHOLD);
    int minutesUntilDue = (int) Duration.between(now, task.getDueDate()).toMinutes();

    long delay = Duration.between(now, reminderTime).toMillis();
    if (delay <= 0) {
      notifyTaskReminder(taskId, minutesUntilDue);
      return;
    }

    Timer timer = new Timer();
    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        notifyTaskReminder(taskId, minutesUntilDue);
        timers.remove(taskId);
      }
    }, delay);

    timers.put(taskId, timer);
  }

  public void cancelReminder(Long taskId) {
    if (timers.containsKey(taskId)) {
      timers.get(taskId).cancel();
      timers.remove(taskId);
    }
  }

  private void notifyTaskReminder(long taskId, int reminderTime) {
    // find owner of project that task belongs to
    Task task = taskRepository.findById(taskId)
        .orElseThrow(() -> new EntityNotFoundException("Task not found"));
    Project project = task.getPhase().getProject();
    DevLogger.logToFile("notify task reminder for task: " + task.getTaskName() + " in project: " + project.getId());
    User owner = project.getOwner();
    notificationService.notifyTaskReminder(owner, task, reminderTime, project);
    // notify to user who is assigned to task
    User assignedTo = task.getAssignedTo();
    if (assignedTo != null && assignedTo.getId() != owner.getId()) {
      notificationService.notifyTaskReminder(assignedTo, task, reminderTime, project);
    }
  }
}
