package org.project.collab.project.repository;
import java.util.Optional; import org.project.collab.project.domain.Project; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import jakarta.persistence.LockModeType;
public interface ProjectRepository extends JpaRepository<Project,Long>{
 @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select p from Project p where p.id=:id") Optional<Project> findByIdForUpdate(@Param("id")Long id);
 @Query(value="select count(*) from tasks where project_id=:id",nativeQuery=true) long countTasks(@Param("id")Long id);
}
