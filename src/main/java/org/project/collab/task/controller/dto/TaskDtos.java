package org.project.collab.task.controller.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.*;
import org.project.collab.task.domain.*;
import org.project.collab.user.domain.User;

public final class TaskDtos {
  private TaskDtos() {}

  public record Create(
      @NotBlank @Size(max = 200) String title,
      @Size(max = 2000) String description,
      Long assigneeId,
      TaskStatus status) {}

  public static class Update {
    private final Set<String> present = new HashSet<>();
    @NotNull private Long version;

    @Size(max = 200)
    private String title;

    @Size(max = 2000)
    private String description;

    private Long assigneeId;
    private TaskStatus status;

    public Long getVersion() {
      return version;
    }

    public void setVersion(Long v) {
      version = v;
    }

    public String getTitle() {
      return title;
    }

    public void setTitle(String v) {
      title = v;
      present.add("title");
    }

    public String getDescription() {
      return description;
    }

    public void setDescription(String v) {
      description = v;
      present.add("description");
    }

    public Long getAssigneeId() {
      return assigneeId;
    }

    public void setAssigneeId(Long v) {
      assigneeId = v;
      present.add("assigneeId");
    }

    public TaskStatus getStatus() {
      return status;
    }

    public void setStatus(TaskStatus v) {
      status = v;
      present.add("status");
    }

    public boolean has(String f) {
      return present.contains(f);
    }

    public boolean hasChanges() {
      return !present.isEmpty();
    }
  }

  public record Assignee(Long userId, String name) {
    static Assignee from(User u) {
      return u == null ? null : new Assignee(u.getId(), u.getName());
    }
  }

  public record Detail(
      Long id,
      Long projectId,
      String title,
      String description,
      Assignee assignee,
      TaskStatus status,
      Long version,
      LocalDateTime createdAt,
      LocalDateTime updatedAt) {
    public static Detail from(Task t) {
      return new Detail(
          t.getId(),
          t.getProject().getId(),
          t.getTitle(),
          t.getDescription(),
          Assignee.from(t.getAssignee()),
          t.getStatus(),
          t.getVersion(),
          t.getCreatedAt(),
          t.getUpdatedAt());
    }
  }

  public record Summary(
      Long id,
      String title,
      Assignee assignee,
      TaskStatus status,
      Long version,
      LocalDateTime createdAt,
      LocalDateTime updatedAt) {
    public static Summary from(Task t) {
      return new Summary(
          t.getId(),
          t.getTitle(),
          Assignee.from(t.getAssignee()),
          t.getStatus(),
          t.getVersion(),
          t.getCreatedAt(),
          t.getUpdatedAt());
    }
  }
}
