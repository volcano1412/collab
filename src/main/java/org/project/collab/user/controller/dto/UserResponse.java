package org.project.collab.user.controller.dto;
import java.time.LocalDateTime; import org.project.collab.user.domain.User;
public record UserResponse(Long id,String email,String name,LocalDateTime createdAt){public static UserResponse from(User u){return new UserResponse(u.getId(),u.getEmail(),u.getName(),u.getCreatedAt());}}
