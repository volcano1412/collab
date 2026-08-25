package org.project.collab.task.service;
import java.util.Optional;
public interface MembershipPort {Optional<Membership> find(Long projectId,Long userId);record Membership(boolean canManageAnyTask){} }
