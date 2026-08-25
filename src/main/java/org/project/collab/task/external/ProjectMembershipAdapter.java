package org.project.collab.task.external;
import java.util.Optional; import org.project.collab.project.service.ProjectMemberService; import org.project.collab.task.service.MembershipPort; import org.springframework.stereotype.Component;
@Component public class ProjectMembershipAdapter implements MembershipPort {private final ProjectMemberService service;public ProjectMembershipAdapter(ProjectMemberService s){service=s;}public Optional<Membership> find(Long p,Long u){return service.findRole(p,u).map(r->new Membership(r.canManageAnyTask()));}}
