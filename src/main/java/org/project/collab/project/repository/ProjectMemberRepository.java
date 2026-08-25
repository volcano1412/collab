package org.project.collab.project.repository;

import java.util.*;
import org.project.collab.project.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {
  Optional<ProjectMember> findByProjectIdAndUserId(Long projectId, Long userId);

  boolean existsByProjectIdAndUserId(Long projectId, Long userId);

  long countByProjectId(Long projectId);

  long countByProjectIdAndRole(Long projectId, ProjectRole role);

  @Query(
      "select m from ProjectMember m join fetch m.user where m.project.id=:projectId order by"
          + " m.joinedAt,m.id")
  List<ProjectMember> findAllWithUser(@Param("projectId") Long projectId);

  @Query(
      "select m from ProjectMember m join fetch m.project where m.user.id=:userId order by"
          + " m.project.createdAt desc,m.project.id desc")
  List<ProjectMember> findProjectsForUser(@Param("userId") Long userId);
}
