package org.project.collab.common.error;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
  INVALID_REQUEST(HttpStatus.BAD_REQUEST, "요청 값이 올바르지 않습니다."),
  MISSING_USER_HEADER(HttpStatus.BAD_REQUEST, "X-User-Id 헤더가 필요합니다."),
  ASSIGNEE_NOT_MEMBER(HttpStatus.BAD_REQUEST, "담당자는 프로젝트 멤버여야 합니다."),
  USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
  EMAIL_DUPLICATED(HttpStatus.CONFLICT, "이미 등록된 이메일입니다."),
  PROJECT_NOT_FOUND(HttpStatus.NOT_FOUND, "프로젝트를 찾을 수 없습니다."),
  PROJECT_FORBIDDEN(HttpStatus.FORBIDDEN, "프로젝트 권한이 없습니다."),
  MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "멤버를 찾을 수 없습니다."),
  MEMBER_DUPLICATED(HttpStatus.CONFLICT, "이미 프로젝트 멤버입니다."),
  LAST_OWNER_REQUIRED(HttpStatus.CONFLICT, "프로젝트에는 최소 한 명의 OWNER가 필요합니다."),
  OWNER_ROLE_FORBIDDEN(HttpStatus.FORBIDDEN, "OWNER 역할을 변경할 권한이 없습니다."),
  TASK_NOT_FOUND(HttpStatus.NOT_FOUND, "작업을 찾을 수 없습니다."),
  TASK_FORBIDDEN(HttpStatus.FORBIDDEN, "작업을 변경할 권한이 없습니다."),
  TASK_VERSION_CONFLICT(HttpStatus.CONFLICT, "다른 사용자가 먼저 수정했습니다. 최신 내용을 확인해 주세요."),
  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");
  private final HttpStatus status;
  private final String message;

  ErrorCode(HttpStatus status, String message) {
    this.status = status;
    this.message = message;
  }

  public HttpStatus status() {
    return status;
  }

  public String message() {
    return message;
  }
}
