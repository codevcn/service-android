package com.example.taskmanager.controller;

import com.example.taskmanager.dev.DevLogger;
import com.example.taskmanager.dto.ApiResponse;
import com.example.taskmanager.dto.TaskDTO;
import com.example.taskmanager.dto.TaskMemberDTO;
import com.example.taskmanager.model.User;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.Phase;
import com.example.taskmanager.model.Project;
import com.example.taskmanager.model.ProjectMember;
import com.example.taskmanager.repository.PhaseRepository;
import com.example.taskmanager.repository.ProjectMemberRepository;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.repository.UserRepository;
import com.example.taskmanager.service.NotificationService;
import com.example.taskmanager.service.TaskReminderManager;
import com.example.taskmanager.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Tasks", description = "Task management APIs")
public class TaskController {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private PhaseRepository phaseRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskReminderManager taskReminderManager;

    @Autowired
    private ProjectMemberRepository projectMemberRepository;

    @Autowired
    private NotificationService notificationService;

    @PostMapping
    public ResponseEntity<ApiResponse> createTask(@RequestBody(required = true) CreateTaskRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Task taskRequest = request.task();
        Long projectId = request.projectId();

        var user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        var role = userService.getRoleInProject(user.getId(), projectId);
        if (role != ProjectMember.Role.Admin && role != ProjectMember.Role.Leader) {
            throw new EntityNotFoundException("User has no permission to create task");
        }

        // kiểm tra xem có thiếu input không
        if (taskRequest.getTaskName() == null || taskRequest.getPhase() == null) {
            throw new IllegalArgumentException("Missing required fields");
        }
        if (taskRequest.getPhase().getId() == null) {
            throw new IllegalArgumentException("Phase ID is required");
        }
        if (taskRequest.getOrderIndex() == null) {
            throw new IllegalArgumentException("Order index is required");
        }

        // Fetch the phase from database to ensure relationships are loaded
        Phase phase = phaseRepository.findById(taskRequest.getPhase().getId())
                .orElseThrow(() -> new EntityNotFoundException("Phase not found"));

        Task task = new Task();
        task.setTaskName(taskRequest.getTaskName());
        task.setPhase(phase); // Use the fetched phase instead of the request phase
        task.setOrderIndex(taskRequest.getOrderIndex());

        Task savedTask = taskRepository.save(task);

        Project project = savedTask.getPhase().getProject();
        List<ProjectMember> projectMembers = projectMemberRepository.findByProject_Id(project.getId());
        for (ProjectMember projectMember : projectMembers) {
            notificationService.notifyGeneral(projectMember.getUser(),
                    "New task created: " + taskRequest.getTaskName());
        }

        return ResponseEntity
                .ok(new ApiResponse("success", TaskDTO.fromEntity(savedTask), null));
    }

    @GetMapping("/phase/{phaseId}")
    public ResponseEntity<ApiResponse> getTasksByPhase(@PathVariable Long phaseId) {
        List<Task> tasks = taskRepository.findByPhaseIdOrderByOrderIndexAsc(phaseId);
        return ResponseEntity.ok(new ApiResponse("success", tasks, null));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getTask(@PathVariable Long id) {
        return taskRepository.findById(id)
                .map(task -> ResponseEntity
                        .ok(new ApiResponse("success", TaskDTO.fromEntity(
                                task), null)))
                .orElse(ResponseEntity.ok(new ApiResponse("error", "Task not found", null)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateTask(@PathVariable Long id,
            @RequestBody(required = false) UpdateTaskRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Task taskRequest = request.task();
        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Task not found"));
        Long projectId = request.projectId();
        var user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        var role = userService.getRoleInProject(user.getId(), projectId);
        if (role != ProjectMember.Role.Admin && role != ProjectMember.Role.Leader) {
            throw new EntityNotFoundException("User has no permission to update task");
        }

        boolean isUpdatingDueDate = false;

        if (taskRequest.getTaskName() != null) {
            existingTask.setTaskName(taskRequest.getTaskName());
        }
        if (taskRequest.getDescription() != null) {
            existingTask.setDescription(taskRequest.getDescription());
        }
        if (taskRequest.getPhase() != null) {
            existingTask.setPhase(taskRequest.getPhase());
        }
        if (taskRequest.getAssignedTo() != null) {
            existingTask.setAssignedTo(taskRequest.getAssignedTo());
        }
        if (taskRequest.getStatus() != null) {
            existingTask.setStatus(taskRequest.getStatus());
        }
        if (taskRequest.getPriority() != null) {
            existingTask.setPriority(taskRequest.getPriority());
        }
        if (taskRequest.getDueDate() != null) {
            existingTask.setDueDate(taskRequest.getDueDate());
            taskReminderManager.scheduleReminder(existingTask);
            isUpdatingDueDate = true;
            List<ProjectMember> projectMembers = projectMemberRepository
                    .findByProject_IdAndRoleIn(existingTask.getPhase().getProject().getId(),
                            List.of(ProjectMember.Role.Admin, ProjectMember.Role.Leader));
            for (ProjectMember projectMember : projectMembers) {
                notificationService.notifyGeneral(projectMember.getUser(),
                        "Task updated: " + existingTask.getTaskName());
            }
            User assignedTo = existingTask.getAssignedTo();
            if (assignedTo != null) {
                notificationService.notifyGeneral(assignedTo,
                        "Task updated: " + existingTask.getTaskName());
            }
        }
        if (taskRequest.getOrderIndex() != null) {
            existingTask.setOrderIndex(taskRequest.getOrderIndex());
        }
        existingTask.setAllowSelfAssign(taskRequest.isAllowSelfAssign() || false);
        taskRepository.save(existingTask);

        if (!isUpdatingDueDate) {
            List<ProjectMember> projectMembers = projectMemberRepository
                    .findByProject_IdAndRoleIn(existingTask.getPhase().getProject().getId(),
                            List.of(ProjectMember.Role.Admin, ProjectMember.Role.Leader));
            for (ProjectMember projectMember : projectMembers) {
                notificationService.notifyGeneral(projectMember.getUser(),
                        "Task updated: " + existingTask.getTaskName());
            }
            User assignedTo = existingTask.getAssignedTo();
            if (assignedTo != null) {
                notificationService.notifyGeneral(assignedTo,
                        "Task updated: " + existingTask.getTaskName());
            }
        }

        return ResponseEntity
                .ok(new ApiResponse("success", "Task updated successfully", null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteTask(@PathVariable Long id,
            @RequestParam Long projectId,
            @AuthenticationPrincipal UserDetails userDetails) {
        var user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        var role = userService.getRoleInProject(user.getId(), projectId);
        if (role != ProjectMember.Role.Admin && role != ProjectMember.Role.Leader) {
            throw new EntityNotFoundException("User has no permission to delete task");
        }
        return taskRepository.findById(id)
                .map(task -> {
                    taskRepository.delete(task);

                    List<ProjectMember> projectMembers = projectMemberRepository
                            .findByProject_IdAndRoleIn(task.getPhase().getProject().getId(),
                                    List.of(ProjectMember.Role.Admin, ProjectMember.Role.Leader));
                    for (ProjectMember projectMember : projectMembers) {
                        notificationService.notifyGeneral(projectMember.getUser(),
                                "Task updated: " + task.getTaskName());
                    }

                    return ResponseEntity.ok(new ApiResponse("success", "Task deleted successfully", null));
                })
                .orElse(ResponseEntity.ok(new ApiResponse("error", "Task not found", null)));
    }

    private void reorderTasksInPhase(Long phaseId, Task taskToMove, int newPosition) {
        List<Task> tasks = taskRepository.findByPhaseIdOrderByOrderIndexAsc(phaseId);
        ArrayList<Task> reorderedTasks = new ArrayList<>();
        for (Task task : tasks) {
            if (task.getId().equals(taskToMove.getId())) {
                continue;
            }
            reorderedTasks.add(task);
        }
        if (newPosition >= 0) {
            if (newPosition > reorderedTasks.size()) {
                newPosition = reorderedTasks.size();
            }
            reorderedTasks.add(newPosition, taskToMove);
        }
        int index = 0;
        for (Task task : reorderedTasks) {
            task.setOrderIndex(index);
            index++;
        }
        taskRepository.saveAll(reorderedTasks);
    }

    @PutMapping("/{taskId}/move")
    public ResponseEntity<ApiResponse> moveTask(@PathVariable Long taskId, @RequestParam Long phaseId,
            @RequestParam Long position,
            @RequestParam Long projectId,
            @AuthenticationPrincipal UserDetails userDetails) {
        var user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        var role = userService.getRoleInProject(user.getId(), projectId);
        if (role != ProjectMember.Role.Admin && role != ProjectMember.Role.Leader) {
            throw new EntityNotFoundException("User has no permission to move task");
        }

        Task taskToMove = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found"));

        Phase newPhase = phaseRepository.findById(phaseId)
                .orElseThrow(() -> new EntityNotFoundException("Phase not found"));

        boolean isSamePhase = taskToMove.getPhase().getId().equals(phaseId);
        int newPosition = position.intValue();

        if (isSamePhase) {
            reorderTasksInPhase(phaseId, taskToMove, newPosition);
        } else {
            reorderTasksInPhase(taskToMove.getPhase().getId(), taskToMove, -1);

            taskToMove.setPhase(newPhase);
            taskToMove.setOrderIndex(newPosition);
            reorderTasksInPhase(phaseId, taskToMove, newPosition);
        }

        return ResponseEntity.ok(new ApiResponse("success", "Task moved successfully", null));
    }

    // get task members by task id
    @GetMapping("/{taskId}/members")
    public ResponseEntity<ApiResponse> getTaskMembers(@PathVariable Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found"));
        ArrayList<TaskMemberDTO> members = new ArrayList<>();
        User userAssignedTo = task.getAssignedTo();
        if (userAssignedTo == null) {
            return ResponseEntity.ok(new ApiResponse("success", members, null));
        }
        ProjectMember projectMember = projectMemberRepository
                .findByUser_IdAndProject_Id(userAssignedTo.getId(),
                        task.getPhase().getProject().getId())
                .orElseThrow(() -> new EntityNotFoundException("Project member not found"));
        members.add(TaskMemberDTO.fromEntity(userAssignedTo, projectMember));
        return ResponseEntity.ok(new ApiResponse("success", members, null));
    }

    // mark task as completed
    @PutMapping("/{id}/mark-as-complete")
    public ResponseEntity<ApiResponse> markTaskAsCompleted(@PathVariable Long id, @RequestParam String status) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Task not found"));
        task.setStatus(status);
        taskRepository.save(task);

        List<ProjectMember> projectMembers = projectMemberRepository
                .findByProject_IdAndRoleIn(task.getPhase().getProject().getId(),
                        List.of(ProjectMember.Role.Admin, ProjectMember.Role.Leader));
        for (ProjectMember projectMember : projectMembers) {
            notificationService.notifyGeneral(projectMember.getUser(), "Task updated: " + task.getTaskName());
        }
        User assignedTo = task.getAssignedTo();
        if (assignedTo != null) {
            notificationService.notifyGeneral(assignedTo, "Task updated: " + task.getTaskName());
        }

        return ResponseEntity.ok(new ApiResponse("success", "Task marked as completed", null));
    }

    private record CreateTaskRequest(Task task, Long projectId) {
    }

    private record UpdateTaskRequest(Task task, Long projectId) {
    }

}