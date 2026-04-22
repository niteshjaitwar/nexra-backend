package com.nexra.hrms.nexra.modules.payroll.exception;

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
 * Centralized exception handling for payroll APIs inside the modular monolith.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 100)
@RestControllerAdvice(basePackages = "com.nexra.hrms.nexra.modules.payroll")
@Slf4j
public class PayrollGlobalExceptionHandler {

    @ExceptionHandler(PayrollBusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(final PayrollBusinessException ex) {
        log.warn("PayrollGlobalExceptionHandler - business error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.failure("BUSINESS_RULE_VIOLATION", ex.getMessage()));
    }

    @ExceptionHandler(PayrollResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(final PayrollResourceNotFoundException ex) {
        log.warn("PayrollGlobalExceptionHandler - resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.failure("NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(PayrollForbiddenException.class)
    public ResponseEntity<ApiResponse<Void>> handleForbidden(final PayrollForbiddenException ex) {
        log.warn("PayrollGlobalExceptionHandler - forbidden: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.failure("FORBIDDEN", ex.getMessage()));
    }

    @ExceptionHandler(PayrollUnauthorizedException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorized(final PayrollUnauthorizedException ex) {
        log.warn("PayrollGlobalExceptionHandler - unauthorized: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.failure("UNAUTHORIZED", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(final MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        log.warn("PayrollGlobalExceptionHandler - request validation failed: {}", errors);
        return ResponseEntity.badRequest()
            .body(ApiResponse.failure("VALIDATION_FAILED", "Validation failed.").withMeta("errors", errors));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(final ConstraintViolationException ex) {
        log.warn("PayrollGlobalExceptionHandler - constraint violation: {}", ex.getMessage());
        return ResponseEntity.badRequest()
            .body(ApiResponse.failure("VALIDATION_FAILED", "Validation failed.")
                .withMeta("errors", Map.of("error", ex.getMessage())));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotReadable(final HttpMessageNotReadableException ex) {
        log.warn("PayrollGlobalExceptionHandler - malformed payload: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(ApiResponse.failure("MALFORMED_JSON", "Invalid request payload."));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(final IllegalArgumentException ex) {
        log.warn("PayrollGlobalExceptionHandler - illegal argument: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(ApiResponse.failure("VALIDATION_FAILED", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnknown(final Exception ex) {
        log.error("PayrollGlobalExceptionHandler - unhandled exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.failure("INTERNAL_ERROR", "Internal server error."));
    }
}
