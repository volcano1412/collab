package org.project.collab;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CollabApiIntegrationTest {
  @Autowired MockMvc mvc;
  @Autowired ObjectMapper mapper;

  @Test
  void userRegistrationAndRequesterValidation() throws Exception {
    mvc.perform(
            post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"owner@example.com\",\"name\":\"Owner\"}"))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "/api/users/1"))
        .andExpect(jsonPath("$.id").value(1));

    mvc.perform(get("/api/users/1"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("MISSING_USER_HEADER"));
    mvc.perform(get("/api/users/1").header("X-User-Id", 99))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"));
  }

  @Test
  void userIsLookedUpByEmailWithoutRequesterHeader() throws Exception {
    long id = createUser("owner@example.com", "Owner");

    // 로그인을 대신하는 진입점이라 X-User-Id 없이 호출된다.
    mvc.perform(get("/api/users").param("email", "owner@example.com"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(id))
        .andExpect(jsonPath("$.name").value("Owner"));

    mvc.perform(get("/api/users").param("email", "nobody@example.com"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"));

    mvc.perform(get("/api/users"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
  }

  @Test
  void projectMembershipPermissionsAndLastOwnerAreEnforced() throws Exception {
    long owner = createUser("owner@example.com", "Owner");
    long admin = createUser("admin@example.com", "Admin");
    long project = createProject(owner, "Alpha");

    mvc.perform(
            post("/api/projects/{id}/members", project)
                .header("X-User-Id", owner)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":" + admin + ",\"role\":\"ADMIN\"}"))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "/api/projects/" + project + "/members/" + admin));

    mvc.perform(
            patch("/api/projects/{id}/members/{userId}", project, owner)
                .header("X-User-Id", admin)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"role\":\"MEMBER\"}"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("OWNER_ROLE_FORBIDDEN"));

    mvc.perform(
            delete("/api/projects/{id}/members/{userId}", project, owner)
                .header("X-User-Id", owner))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("LAST_OWNER_REQUIRED"));
  }

  @Test
  void taskPatchDistinguishesNullAndRejectsStaleVersion() throws Exception {
    long owner = createUser("owner@example.com", "Owner");
    long project = createProject(owner, "Alpha");
    long task = createTask(owner, project, "First");

    mvc.perform(
            patch("/api/projects/{p}/tasks/{t}", project, task)
                .header("X-User-Id", owner)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"description\":null,\"version\":0}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.description").doesNotExist())
        .andExpect(jsonPath("$.version").value(1));

    mvc.perform(
            patch("/api/projects/{p}/tasks/{t}", project, task)
                .header("X-User-Id", owner)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"stale\",\"version\":0}"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("TASK_VERSION_CONFLICT"))
        .andExpect(jsonPath("$.currentVersion").value(1));
  }

  @Test
  void invalidTaskQueryIsAUniformBadRequest() throws Exception {
    long owner = createUser("owner@example.com", "Owner");
    long project = createProject(owner, "Alpha");

    mvc.perform(
            get("/api/projects/{p}/tasks", project)
                .header("X-User-Id", owner)
                .param("status", "UNKNOWN"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
  }

  @Test
  void removingMemberUnassignsTasksAndAdvancesVersion() throws Exception {
    long owner = createUser("owner@example.com", "Owner");
    long member = createUser("member@example.com", "Member");
    long project = createProject(owner, "Alpha");
    mvc.perform(
            post("/api/projects/{p}/members", project)
                .header("X-User-Id", owner)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":" + member + "}"))
        .andExpect(status().isCreated());
    String taskBody =
        mvc.perform(
                post("/api/projects/{p}/tasks", project)
                    .header("X-User-Id", owner)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"title\":\"Assigned\",\"assigneeId\":" + member + "}"))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    long task = mapper.readTree(taskBody).get("id").asLong();

    mvc.perform(delete("/api/projects/{p}/members/{u}", project, member).header("X-User-Id", owner))
        .andExpect(status().isNoContent());
    mvc.perform(get("/api/projects/{p}/tasks/{t}", project, task).header("X-User-Id", owner))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.assignee").doesNotExist())
        .andExpect(jsonPath("$.version").value(1));
  }

  @Test
  void nonMemberCannotDiscoverProjectTasks() throws Exception {
    long owner = createUser("owner@example.com", "Owner");
    long outsider = createUser("outsider@example.com", "Outsider");
    long project = createProject(owner, "Alpha");
    mvc.perform(get("/api/projects/{p}/tasks", project).header("X-User-Id", outsider))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("PROJECT_NOT_FOUND"));
  }

  @Test
  void openApiPublishesEverySeventeenOperations() throws Exception {
    String body =
        mvc.perform(get("/v3/api-docs"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.info.title").value("Collab API"))
            .andReturn()
            .getResponse()
            .getContentAsString();
    JsonNode paths = mapper.readTree(body).get("paths");
    int operations = 0;
    for (JsonNode path : paths) operations += path.size();
    assertThat(operations).isEqualTo(17);
  }

  @Test
  void openApiPublishesCurrentUserAsRequiredHeaderInsteadOfQueryParameter() throws Exception {
    String body =
        mvc.perform(get("/v3/api-docs"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    JsonNode paths = mapper.readTree(body).get("paths");

    int securedOperations = 0;
    for (var pathIterator = paths.fields(); pathIterator.hasNext(); ) {
      var path = pathIterator.next();
      for (var operationIterator = path.getValue().fields(); operationIterator.hasNext(); ) {
        var operation = operationIterator.next();
        // 등록(POST /api/users)과 이메일 조회(GET /api/users)는 로그인 이전 진입점이라 헤더를 요구하지 않는다.
        boolean beforeSignIn =
            path.getKey().equals("/api/users")
                && (operation.getKey().equals("post") || operation.getKey().equals("get"));
        JsonNode parameters = operation.getValue().path("parameters");

        boolean hasUserHeader = false;
        for (JsonNode parameter : parameters) {
          String name = parameter.path("name").asText();
          String location = parameter.path("in").asText();
          assertThat(location.equals("query") && (name.equals("u") || name.equals("requester")))
              .as("custom current-user parameter must not leak as query parameter")
              .isFalse();
          if (name.equals("X-User-Id") && location.equals("header")) {
            hasUserHeader = parameter.path("required").asBoolean();
          }
        }

        if (beforeSignIn) assertThat(hasUserHeader).isFalse();
        else {
          assertThat(hasUserHeader)
              .as(operation.getKey().toUpperCase() + " " + path.getKey())
              .isTrue();
          securedOperations++;
        }
      }
    }
    assertThat(securedOperations).isEqualTo(15);
  }

  private long createUser(String email, String name) throws Exception {
    String body =
        mvc.perform(
                post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"email\":\"" + email + "\",\"name\":\"" + name + "\"}"))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    return mapper.readTree(body).get("id").asLong();
  }

  private long createProject(long userId, String name) throws Exception {
    String body =
        mvc.perform(
                post("/api/projects")
                    .header("X-User-Id", userId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"name\":\"" + name + "\"}"))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    return mapper.readTree(body).get("id").asLong();
  }

  private long createTask(long userId, long projectId, String title) throws Exception {
    String body =
        mvc.perform(
                post("/api/projects/{id}/tasks", projectId)
                    .header("X-User-Id", userId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"title\":\"" + title + "\",\"description\":\"x\"}"))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    return mapper.readTree(body).get("id").asLong();
  }
}
