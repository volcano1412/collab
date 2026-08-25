package org.project.collab.task.service;

import java.util.Optional;
import org.project.collab.common.error.OptimisticLockVersionReader;
import org.project.collab.task.domain.Task;
import org.project.collab.task.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskConflictVersionReader implements OptimisticLockVersionReader {
  private final TaskRepository tasks;

  public TaskConflictVersionReader(TaskRepository tasks) {
    this.tasks = tasks;
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
  public Optional<Long> read(Long id) {
    return tasks.findVersion(id);
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
  public Optional<Long> read(Class<?> entityType, Object identifier) {
    if (entityType == null
        || !Task.class.isAssignableFrom(entityType)
        || !(identifier instanceof Long id)) {
      return Optional.empty();
    }
    return tasks.findVersion(id);
  }
}
