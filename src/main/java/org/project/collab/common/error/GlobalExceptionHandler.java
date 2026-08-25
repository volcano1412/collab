package org.project.collab.common.error;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
  private final OptimisticLockVersionReader versionReader;

  public GlobalExceptionHandler(OptimisticLockVersionReader versionReader) {
    this.versionReader = versionReader;
  }

  @ExceptionHandler(BusinessException.class)
  ResponseEntity<ErrorResponse> business(BusinessException e, HttpServletRequest request) {
    var code = e.getCode();
    return ResponseEntity.status(code.status())
        .body(
            new ErrorResponse(
                code.name(),
                code.message(),
                request.getRequestURI(),
                LocalDateTime.now(),
                null,
                e.getCurrentVersion()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<ErrorResponse> validation(
      MethodArgumentNotValidException e, HttpServletRequest request) {
    List<ErrorResponse.FieldError> errors =
        e.getBindingResult().getFieldErrors().stream()
            .map(error -> new ErrorResponse.FieldError(error.getField(), error.getDefaultMessage()))
            .toList();
    return invalid(request, errors);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  ResponseEntity<ErrorResponse> unreadable(
      HttpMessageNotReadableException e, HttpServletRequest request) {
    return invalid(request, null);
  }

  @ExceptionHandler({
    MethodArgumentTypeMismatchException.class,
    MissingServletRequestParameterException.class
  })
  ResponseEntity<ErrorResponse> requestParameter(Exception e, HttpServletRequest request) {
    return invalid(request, null);
  }

  @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
  ResponseEntity<ErrorResponse> optimisticLock(
      ObjectOptimisticLockingFailureException e, HttpServletRequest request) {
    var currentVersion = versionReader.read(e.getPersistentClass(), e.getIdentifier());
    if (currentVersion.isEmpty()) {
      return error(ErrorCode.TASK_NOT_FOUND, request);
    }

    var code = ErrorCode.TASK_VERSION_CONFLICT;
    return ResponseEntity.status(code.status())
        .body(
            new ErrorResponse(
                code.name(),
                code.message(),
                request.getRequestURI(),
                LocalDateTime.now(),
                null,
                currentVersion.get()));
  }

  @ExceptionHandler(Exception.class)
  ResponseEntity<ErrorResponse> unexpected(Exception e, HttpServletRequest request) {
    log.error("Unhandled exception: {} {}", request.getMethod(), request.getRequestURI(), e);
    return error(ErrorCode.INTERNAL_SERVER_ERROR, request);
  }

  private ResponseEntity<ErrorResponse> invalid(
      HttpServletRequest request, List<ErrorResponse.FieldError> errors) {
    var code = ErrorCode.INVALID_REQUEST;
    return ResponseEntity.badRequest()
        .body(
            new ErrorResponse(
                code.name(),
                code.message(),
                request.getRequestURI(),
                LocalDateTime.now(),
                errors,
                null));
  }

  private ResponseEntity<ErrorResponse> error(ErrorCode code, HttpServletRequest request) {
    return ResponseEntity.status(code.status())
        .body(
            new ErrorResponse(
                code.name(),
                code.message(),
                request.getRequestURI(),
                LocalDateTime.now(),
                null,
                null));
  }
}
