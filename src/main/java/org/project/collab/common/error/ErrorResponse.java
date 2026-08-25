package org.project.collab.common.error;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.List;
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(String code,String message,String path,LocalDateTime timestamp,List<FieldError> errors,Long currentVersion) {
    public record FieldError(String field,String reason) {}
}
