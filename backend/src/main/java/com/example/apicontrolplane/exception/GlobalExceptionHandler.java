package com.example.apicontrolplane.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(ApiException.class)
  ResponseEntity<Map<String, Object>> api(ApiException ex, HttpServletRequest request) {
    return body(ex.status(), ex.getMessage(), request.getRequestURI(), null);
  }

  @ExceptionHandler(IllegalStateException.class)
  ResponseEntity<Map<String, Object>> illegalState(IllegalStateException ex, HttpServletRequest request) {
    return body(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI(), null);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<Map<String, Object>> validation(MethodArgumentNotValidException ex, HttpServletRequest request) {
    Map<String, String> fields = ex.getBindingResult().getFieldErrors().stream()
        .collect(java.util.stream.Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage, (a, b) -> a));
    return body(HttpStatus.BAD_REQUEST, "Validation failed", request.getRequestURI(), fields);
  }

  private ResponseEntity<Map<String, Object>> body(HttpStatus status, String message, String path, Object details) {
    return ResponseEntity.status(status).body(Map.of(
        "timestamp", Instant.now().toString(),
        "status", status.value(),
        "error", status.getReasonPhrase(),
        "message", message,
        "path", path,
        "details", details == null ? Map.of() : details));
  }
}
