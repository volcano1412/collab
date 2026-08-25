package org.project.collab.project.controller.dto;
import jakarta.validation.constraints.*; import java.time.LocalDateTime; import java.util.*; import org.project.collab.project.domain.*;
public final class ProjectDtos {private ProjectDtos(){}
 public record Create(@NotBlank @Size(max=100) String name,@Size(max=500) String description){}
 public static class Update {private final Set<String> present=new HashSet<>(); @Size(max=100) private String name; @Size(max=500) private String description; public void setName(String v){name=v;present.add("name");} public void setDescription(String v){description=v;present.add("description");} public String getName(){return name;} public String getDescription(){return description;} public boolean has(String f){return present.contains(f);} }
 public record Response(Long id,String name,String description,ProjectRole myRole,long memberCount,Long taskCount,LocalDateTime createdAt,LocalDateTime updatedAt){}
 public record MemberCreate(@NotNull Long userId,ProjectRole role){public ProjectRole effectiveRole(){return role==null?ProjectRole.MEMBER:role;}}
 public record MemberUpdate(@NotNull ProjectRole role){}
 public record MemberResponse(Long userId,String name,String email,ProjectRole role,LocalDateTime joinedAt){public static MemberResponse from(ProjectMember m){return new MemberResponse(m.getUser().getId(),m.getUser().getName(),m.getUser().getEmail(),m.getRole(),m.getJoinedAt());}}
}
