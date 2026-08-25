package org.project.collab.user.controller;

import jakarta.validation.Valid;
import java.net.URI;
import org.project.collab.common.web.CurrentUser;
import org.project.collab.user.controller.dto.*;
import org.project.collab.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
  private final UserService service;

  public UserController(UserService s) {
    service = s;
  }

  @PostMapping
  public ResponseEntity<UserResponse> create(@Valid @RequestBody UserCreateRequest r) {
    var u = service.create(r.email(), r.name());
    return ResponseEntity.created(URI.create("/api/users/" + u.getId())).body(UserResponse.from(u));
  }

  /**
   * 이메일로 조회한다. 로그인을 대신하는 진입점이라 X-User-Id 를 요구하지 않는다 — 요청자는 아직 자기 id를 모른다.
   */
  @GetMapping
  public UserResponse getByEmail(@RequestParam String email) {
    return UserResponse.from(service.requireByEmail(email));
  }

  @GetMapping("/{id}")
  public UserResponse get(@CurrentUser Long requester, @PathVariable Long id) {
    return UserResponse.from(service.requireUser(id));
  }
}
