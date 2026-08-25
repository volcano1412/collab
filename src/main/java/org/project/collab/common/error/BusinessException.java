package org.project.collab.common.error;

public class BusinessException extends RuntimeException {
  private final ErrorCode code;
  private final Long currentVersion;

  public BusinessException(ErrorCode code) {
    this(code, null);
  }

  public BusinessException(ErrorCode code, Long currentVersion) {
    super(code.message());
    this.code = code;
    this.currentVersion = currentVersion;
  }

  public ErrorCode getCode() {
    return code;
  }

  public Long getCurrentVersion() {
    return currentVersion;
  }
}
