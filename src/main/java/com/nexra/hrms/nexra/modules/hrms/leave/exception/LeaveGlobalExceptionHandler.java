package com.nexra.hrms.nexra.modules.hrms.leave.exception;

import com.nexra.hrms.nexra.common.api.ApiResponse;
import com.nexra.hrms.nexra.common.exception.ApiErrorResponseFactory;
import jakarta.validation.ConstraintViolationException;
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
 * Centralized exception handling for leave management APIs.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 100)
@RestControllerAdvice(basePackages = "com.nexra.hrms.nexra.modules.hrms.leave")
@Slf4j
public class LeaveGlobalExceptionHandler {

    @ExceptionHandler(LeaveBusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(final LeaveBusinessException ex) {
        log.warn("Leave LeaveGlobalExceptionHandler - business error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ApiErrorResponseFactory.failure("BUSINESS_RULE_VIOLATION", ex.getMessage()));
    }

    @ExceptionHandler(LeaveResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(final LeaveResourceNotFoundException ex) {
        log.warn("Leave LeaveGlobalExceptionHandler - resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiErrorResponseFactory.failure("NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(LeaveForbiddenException.class)
    public ResponseEntity<ApiResponse<Void>> handleForbidden(final LeaveForbiddenException ex) {
        log.warn("Leave LeaveGlobalExceptionHandler - forbidden: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ApiErrorResponseFactory.failure("FORBIDDEN", ex.getMessage()));
    }

    @ExceptionHandler(LeaveUnauthorizedException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorized(final LeaveUnauthorizedException ex) {
        log.warn("Leave LeaveGlobalExceptionHandler - unauthorized: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ApiErrorResponseFactory.failure("UNAUTHORIZED", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(final MethodArgumentNotValidException ex) {
        log.warn("Leave LeaveGlobalExceptionHandler - validation failed: {}", ex.getMessage());
        return ResponseEntity.badRequest()
            .body(ApiErrorResponseFactory.validation(ex, "Validation failed."));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(final ConstraintViolationException ex) {
        log.warn("Leave LeaveGlobalExceptionHandler - constraint violation: {}", ex.getMessage());
        return ResponseEntity.badRequest()
            .body(ApiErrorResponseFactory.validation(ex, "Validation failed."));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotReadable(final HttpMessageNotReadableException ex) {
        log.warn("Leave LeaveGlobalExceptionHandler - malformed payload: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(ApiResponse.failure("MALFORMED_JSON", "Invalid request payload."));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(final IllegalArgumentException ex) {
        log.warn("Leave LeaveGlobalExceptionHandler - illegal argument: {}", ex.getMessage());
        return ResponseEntity.badRequest()
            .body(ApiErrorResponseFactory.failure("VALIDATION_FAILED", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnhandled(final Exception ex) {
        log.error("Leave LeaveGlobalExceptionHandler - unhandled exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiErrorResponseFactory.failure("INTERNAL_ERROR", "Internal server error."));
    }
}
