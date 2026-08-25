package org.project.collab.common.error;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.project.collab.task.domain.Task;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

class GlobalExceptionHandlerTest {
  private MockMvc mvc;

  @BeforeEach
  void setUp() {
    OptimisticLockVersionReader versionReader = mock(OptimisticLockVersionReader.class);
    when(versionReader.read(Task.class, 1L)).thenReturn(Optional.of(5L));
    mvc =
        MockMvcBuilders.standaloneSetup(new FailureController())
            .setControllerAdvice(new GlobalExceptionHandler(versionReader))
            .build();
  }

  @Test
  void optimisticLockFailureIsAUniformConflict() throws Exception {
    mvc.perform(get("/test/optimistic-lock"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("TASK_VERSION_CONFLICT"))
        .andExpect(jsonPath("$.message").value("다른 사용자가 먼저 수정했습니다. 최신 내용을 확인해 주세요."))
        .andExpect(jsonPath("$.path").value("/test/optimistic-lock"))
        .andExpect(jsonPath("$.currentVersion").value(5))
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  void unexpectedFailureIsAUniformInternalServerError() throws Exception {
    mvc.perform(get("/test/unexpected"))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.code").value("INTERNAL_SERVER_ERROR"))
        .andExpect(jsonPath("$.message").value("서버 내부 오류가 발생했습니다."))
        .andExpect(jsonPath("$.path").value("/test/unexpected"))
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @RestController
  static class FailureController {
    @GetMapping("/test/optimistic-lock")
    void optimisticLock() {
      throw new ObjectOptimisticLockingFailureException(Task.class, 1L);
    }

    @GetMapping("/test/unexpected")
    void unexpected() {
      throw new IllegalStateException("sensitive implementation detail");
    }
  }
}
