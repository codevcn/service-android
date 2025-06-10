package com.example.taskmanager.dto;

import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.Project;
import com.example.taskmanager.model.Phase;
import lombok.Data;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class SearchDTO {
  private List<TaskDTO> tasks;
  private List<ProjectDTO> projects;
  private List<PhaseDTO> phases;

  public SearchDTO(List<Task> tasks, List<Project> projects, List<Phase> phases) {
    this.tasks = tasks.stream().map(TaskDTO::fromEntity).collect(Collectors.toList());
    this.projects = projects.stream().map(ProjectDTO::fromEntity).collect(Collectors.toList());
    this.phases = phases.stream().map(PhaseDTO::fromEntity).collect(Collectors.toList());
  }
}