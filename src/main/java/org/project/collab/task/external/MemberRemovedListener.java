package org.project.collab.task.external;

import org.project.collab.project.domain.event.ProjectMemberRemoved;
import org.project.collab.task.domain.Task;
import org.project.collab.task.repository.TaskRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class MemberRemovedListener {
  private final TaskRepository tasks;

  public MemberRemovedListener(TaskRepository t) {
    tasks = t;
  }

  @EventListener
  public void on(ProjectMemberRemoved e) {
    tasks.findByProjectIdAndAssigneeId(e.projectId(), e.userId()).forEach(Task::unassign);
  }
}
