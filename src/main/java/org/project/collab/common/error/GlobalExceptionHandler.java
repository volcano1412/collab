package org.project.collab.common.error;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MissingServletRequestParameterException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    ResponseEntity<ErrorResponse> business(BusinessException e,HttpServletRequest r){
        var c=e.getCode(); return ResponseEntity.status(c.status()).body(new ErrorResponse(c.name(),c.message(),r.getRequestURI(),LocalDateTime.now(),null,e.getCurrentVersion()));
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> validation(MethodArgumentNotValidException e,HttpServletRequest r){
        List<ErrorResponse.FieldError> errors=e.getBindingResult().getFieldErrors().stream().map(x->new ErrorResponse.FieldError(x.getField(),x.getDefaultMessage())).toList();
        return invalid(r,errors);
    }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ErrorResponse> unreadable(HttpMessageNotReadableException e,HttpServletRequest r){return invalid(r,null);}
    @ExceptionHandler({MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class})
    ResponseEntity<ErrorResponse> requestParameter(Exception e,HttpServletRequest r){return invalid(r,null);}
    private ResponseEntity<ErrorResponse> invalid(HttpServletRequest r,List<ErrorResponse.FieldError> errors){
        var c=ErrorCode.INVALID_REQUEST; return ResponseEntity.badRequest().body(new ErrorResponse(c.name(),c.message(),r.getRequestURI(),LocalDateTime.now(),errors,null));
    }
}
