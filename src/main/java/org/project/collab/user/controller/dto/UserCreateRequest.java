package org.project.collab.user.controller.dto;
import jakarta.validation.constraints.*;
public record UserCreateRequest(@NotBlank @Email @Size(max=255) String email,@NotBlank @Size(max=50) String name) {}
