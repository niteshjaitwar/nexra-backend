package com.nexra.hrms.nexra.modules.hrms.leave.exception;

import com.nexra.hrms.nexra.modules.hrms.leave.dto.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Centralized exception handling for leave management APIs.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@RestControllerAdvice(basePackages = "com.nexra.hrms.nexra.modules.hrms.leave")
@Slf4j
public class LeaveGlobalExceptionHandler {

    @ExceptionHandler(LeaveBusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(final LeaveBusinessException ex) {
        log.warn("Leave LeaveGlobalExceptionHandler - business error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.failure(ex.getMessage()));
    }

    @ExceptionHandler(LeaveResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(final LeaveResourceNotFoundException ex) {
        log.warn("Leave LeaveGlobalExceptionHandler - resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.failure(ex.getMessage()));
    }

    @ExceptionHandler(LeaveForbiddenException.class)
    public ResponseEntity<ApiResponse<Void>> handleForbidden(final LeaveForbiddenException ex) {
        log.warn("Leave LeaveGlobalExceptionHandler - forbidden: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.failure(ex.getMessage()));
    }

    @ExceptionHandler(LeaveUnauthorizedException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorized(final LeaveUnauthorizedException ex) {
        log.warn("Leave LeaveGlobalExceptionHandler - unauthorized: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.failure(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleValidation(final MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err -> fieldErrors.put(err.getField(), err.getDefaultMessage()));
        log.warn("Leave LeaveGlobalExceptionHandler - validation failed: {}", fieldErrors);
        return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Validation failed.", Map.of(
            "errors", fieldErrors,
            "timestamp", Instant.now()
        )));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleConstraintViolation(final ConstraintViolationException ex) {
        log.warn("Leave LeaveGlobalExceptionHandler - constraint violation: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Validation failed.", Map.of(
            "details", ex.getMessage(),
            "timestamp", Instant.now()
        )));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleNotReadable(final HttpMessageNotReadableException ex) {
        log.warn("Leave LeaveGlobalExceptionHandler - malformed payload: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Invalid request payload.", Map.of(
            "timestamp", Instant.now()
        )));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(final IllegalArgumentException ex) {
        log.warn("Leave LeaveGlobalExceptionHandler - illegal argument: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(ApiResponse.failure(ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnhandled(final Exception ex) {
        log.error("Leave LeaveGlobalExceptionHandler - unhandled exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.failure("Internal server error."));
    }
}
