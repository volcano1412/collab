package org.project.collab.project.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.project.collab.common.web.CurrentUser;
import org.project.collab.project.controller.dto.ProjectDtos;
import org.project.collab.project.service.ProjectMemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects/{p}/members")
public class ProjectMemberController {
  private final ProjectMemberService service;

  public ProjectMemberController(ProjectMemberService s) {
    service = s;
  }

  @GetMapping
  public List<ProjectDtos.MemberResponse> list(@CurrentUser Long u, @PathVariable Long p) {
    return service.list(u, p);
  }

  @PostMapping
  public ResponseEntity<ProjectDtos.MemberResponse> add(
      @CurrentUser Long u, @PathVariable Long p, @Valid @RequestBody ProjectDtos.MemberCreate r) {
    var x = service.add(u, p, r);
    return ResponseEntity.created(URI.create("/api/projects/" + p + "/members/" + x.userId()))
        .body(x);
  }

  @PatchMapping("/{id}")
  public ProjectDtos.MemberResponse role(
      @CurrentUser Long u,
      @PathVariable Long p,
      @PathVariable Long id,
      @Valid @RequestBody ProjectDtos.MemberUpdate r) {
    return service.changeRole(u, p, id, r.role());
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> remove(
      @CurrentUser Long u, @PathVariable Long p, @PathVariable Long id) {
    service.remove(u, p, id);
    return ResponseEntity.noContent().build();
  }
}
