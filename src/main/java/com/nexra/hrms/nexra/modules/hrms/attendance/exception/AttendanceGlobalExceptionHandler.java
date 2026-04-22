package com.nexra.hrms.nexra.modules.hrms.attendance.exception;

import com.nexra.hrms.nexra.common.api.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Centralized exception handling for attendance APIs.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 100)
@RestControllerAdvice(basePackages = "com.nexra.hrms.nexra.modules.hrms.attendance")
@Slf4j
public class AttendanceGlobalExceptionHandler {

    @ExceptionHandler(AttendanceBusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(final AttendanceBusinessException ex) {
        log.warn("Attendance AttendanceGlobalExceptionHandler - business error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.failure(ex.getMessage()));
    }

    @ExceptionHandler(AttendanceResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(final AttendanceResourceNotFoundException ex) {
        log.warn("Attendance AttendanceGlobalExceptionHandler - resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.failure(ex.getMessage()));
    }

    @ExceptionHandler(AttendanceForbiddenException.class)
    public ResponseEntity<ApiResponse<Void>> handleForbidden(final AttendanceForbiddenException ex) {
        log.warn("Attendance AttendanceGlobalExceptionHandler - forbidden: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.failure(ex.getMessage()));
    }

    @ExceptionHandler(AttendanceUnauthorizedException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorized(final AttendanceUnauthorizedException ex) {
        log.warn("Attendance AttendanceGlobalExceptionHandler - unauthorized: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.failure(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(final MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err -> fieldErrors.put(err.getField(), err.getDefaultMessage()));
        log.warn("Attendance AttendanceGlobalExceptionHandler - request validation failed: {}", fieldErrors);
        return ResponseEntity.badRequest()
            .body(ApiResponse.failure("VALIDATION_FAILED", "Validation failed.").withMeta("errors", fieldErrors));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(final ConstraintViolationException ex) {
        log.warn("Attendance AttendanceGlobalExceptionHandler - constraint violation: {}", ex.getMessage());
        return ResponseEntity.badRequest()
            .body(ApiResponse.failure("VALIDATION_FAILED", "Validation failed.")
                .withMeta("details", ex.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotReadable(final HttpMessageNotReadableException ex) {
        log.warn("Attendance AttendanceGlobalExceptionHandler - malformed payload: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(ApiResponse.failure("MALFORMED_JSON", "Invalid request payload."));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(final IllegalArgumentException ex) {
        log.warn("Attendance AttendanceGlobalExceptionHandler - illegal argument: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(ApiResponse.failure(ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnhandled(final Exception ex) {
        log.error("Attendance AttendanceGlobalExceptionHandler - unhandled exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.failure("Internal server error."));
    }
}
