package org.project.collab.task.service;
import java.util.Optional; import org.project.collab.task.repository.TaskRepository; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.*;
@Service public class TaskConflictVersionReader {private final TaskRepository tasks;public TaskConflictVersionReader(TaskRepository t){tasks=t;}@Transactional(propagation=Propagation.REQUIRES_NEW,readOnly=true) public Optional<Long> read(Long id){return tasks.findVersion(id);}}
