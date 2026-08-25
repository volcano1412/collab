package org.project.collab.task.repository;
import java.util.*; import org.project.collab.task.domain.Task; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param;
public interface TaskRepository extends JpaRepository<Task,Long>,JpaSpecificationExecutor<Task>{Optional<Task> findByIdAndProjectId(Long id,Long projectId);List<Task> findByProjectIdAndAssigneeId(Long projectId,Long assigneeId);@Query("select t.version from Task t where t.id=:id") Optional<Long> findVersion(@Param("id")Long id);}
