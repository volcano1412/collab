package org.project.collab.user.controller;
import jakarta.validation.Valid; import java.net.URI; import org.project.collab.common.web.CurrentUser; import org.project.collab.user.controller.dto.*; import org.project.collab.user.service.UserService; import org.springframework.http.ResponseEntity; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/users") public class UserController {private final UserService service;public UserController(UserService s){service=s;}
 @PostMapping public ResponseEntity<UserResponse> create(@Valid @RequestBody UserCreateRequest r){var u=service.create(r.email(),r.name());return ResponseEntity.created(URI.create("/api/users/"+u.getId())).body(UserResponse.from(u));}
 @GetMapping("/{id}") public UserResponse get(@CurrentUser Long requester,@PathVariable Long id){return UserResponse.from(service.requireUser(id));}}
