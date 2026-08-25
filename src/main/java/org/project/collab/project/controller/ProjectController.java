package org.project.collab.project.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.project.collab.common.web.CurrentUser;
import org.project.collab.project.controller.dto.ProjectDtos;
import org.project.collab.project.service.ProjectService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {
  private final ProjectService service;

  public ProjectController(ProjectService s) {
    service = s;
  }

  @PostMapping
  public ResponseEntity<ProjectDtos.Response> create(
      @CurrentUser Long u, @Valid @RequestBody ProjectDtos.Create r) {
    var x = service.create(u, r);
    return ResponseEntity.created(URI.create("/api/projects/" + x.id())).body(x);
  }

  @GetMapping
  public List<ProjectDtos.Response> list(@CurrentUser Long u) {
    return service.list(u);
  }

  @GetMapping("/{p}")
  public ProjectDtos.Response get(@CurrentUser Long u, @PathVariable Long p) {
    return service.get(u, p);
  }

  @PatchMapping("/{p}")
  public ProjectDtos.Response update(
      @CurrentUser Long u, @PathVariable Long p, @Valid @RequestBody ProjectDtos.Update r) {
    return service.update(u, p, r);
  }

  @DeleteMapping("/{p}")
  public ResponseEntity<Void> delete(@CurrentUser Long u, @PathVariable Long p) {
    service.delete(u, p);
    return ResponseEntity.noContent().build();
  }
}
