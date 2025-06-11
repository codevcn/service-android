package com.example.taskmanager.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.persistence.EntityNotFoundException;

import com.example.taskmanager.dev.DevLogger;
import com.example.taskmanager.model.Project;
import com.example.taskmanager.model.ProjectMember;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.ProjectMemberRepository;
import com.example.taskmanager.repository.ProjectRepository;

@Component
public class ProjectReminderManager {
  private final Map<Long, Timer> timers = new ConcurrentHashMap<>();
  private final int REMINDER_TIME_THRESHOLD = 10;

  @Autowired
  private NotificationService notificationService;

  @Autowired
  private ProjectRepository projectRepository;

  @Autowired
  private ProjectMemberRepository projectMemberRepository;

  public void scheduleReminder(Project project) {
    long projectId = project.getId();

    // Huỷ nếu đã tồn tại
    if (timers.containsKey(projectId)) {
      cancelReminder(projectId);
    }

    LocalDateTime now = LocalDateTime.now();
    LocalDateTime reminderTime = project.getEndDate().minusMinutes(REMINDER_TIME_THRESHOLD);
    int minutesUntilDue = (int) Duration.between(now, project.getEndDate()).toMinutes();

    long delay = Duration.between(now, reminderTime).toMillis();
    if (delay <= 0) {
      notifyProjectReminder(projectId, minutesUntilDue);
      return;
    }

    Timer timer = new Timer();
    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        notifyProjectReminder(projectId, minutesUntilDue);
        timers.remove(projectId);
      }
    }, delay);

    timers.put(projectId, timer);
  }

  public void cancelReminder(Long taskId) {
    if (timers.containsKey(taskId)) {
      timers.get(taskId).cancel();
      timers.remove(taskId);
    }
  }

  private void notifyProjectReminder(long projectId, int reminderTime) {
    // find owner of project that task belongs to
    Project project = projectRepository.findById(projectId)
        .orElseThrow(() -> new EntityNotFoundException("Project not found"));
    User owner = project.getOwner();
    notificationService.notifyProjectReminder(owner, project, reminderTime);
    // notify to user who is assigned to task
    List<ProjectMember> projectMembers = projectMemberRepository.findByProject_Id(projectId);
    for (ProjectMember projectMember : projectMembers) {
      if (projectMember.getUser().getId() != owner.getId()) {
        notificationService.notifyProjectReminder(projectMember.getUser(), project, reminderTime);
      }
    }
  }
}
