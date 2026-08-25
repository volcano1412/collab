package org.project.collab.task.controller;

import jakarta.validation.Valid;
import java.net.URI;
import org.project.collab.common.web.*;
import org.project.collab.task.controller.dto.TaskDtos;
import org.project.collab.task.domain.TaskStatus;
import org.project.collab.task.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects/{p}/tasks")
public class TaskController {
  private final TaskService service;

  public TaskController(TaskService s) {
    service = s;
  }

  @PostMapping
  public ResponseEntity<TaskDtos.Detail> create(
      @CurrentUser Long u, @PathVariable Long p, @Valid @RequestBody TaskDtos.Create r) {
    var x = service.create(u, p, r);
    return ResponseEntity.created(URI.create("/api/projects/" + p + "/tasks/" + x.id())).body(x);
  }

  @GetMapping
  public PageResponse<TaskDtos.Summary> list(
      @CurrentUser Long u,
      @PathVariable Long p,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) TaskStatus status,
      @RequestParam(required = false) Long assigneeId,
      @RequestParam(defaultValue = "false") boolean unassigned,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(defaultValue = "createdAt,desc") String sort) {
    return service.list(u, p, keyword, status, assigneeId, unassigned, page, size, sort);
  }

  @GetMapping("/{t}")
  public TaskDtos.Detail get(@CurrentUser Long u, @PathVariable Long p, @PathVariable Long t) {
    return service.get(u, p, t);
  }

  @PatchMapping("/{t}")
  public TaskDtos.Detail update(
      @CurrentUser Long u,
      @PathVariable Long p,
      @PathVariable Long t,
      @Valid @RequestBody TaskDtos.Update r) {
    return service.update(u, p, t, r);
  }

  @DeleteMapping("/{t}")
  public ResponseEntity<Void> delete(
      @CurrentUser Long u, @PathVariable Long p, @PathVariable Long t) {
    service.delete(u, p, t);
    return ResponseEntity.noContent().build();
  }
}
