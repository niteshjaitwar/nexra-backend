package com.nexra.hrms.nexra.modules.hrms.employee.exception;

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
 * Centralized exception handling for employee-core APIs.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 100)
@RestControllerAdvice(basePackages = "com.nexra.hrms.nexra.modules.hrms.employee")
@Slf4j
public class EmployeeCoreGlobalExceptionHandler {

    @ExceptionHandler(EmployeeCoreBusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(final EmployeeCoreBusinessException ex) {
        log.warn("EmployeeCore EmployeeCoreGlobalExceptionHandler - business error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ApiErrorResponseFactory.failure("BUSINESS_RULE_VIOLATION", ex.getMessage()));
    }

    @ExceptionHandler(EmployeeCoreResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(final EmployeeCoreResourceNotFoundException ex) {
        log.warn("EmployeeCore EmployeeCoreGlobalExceptionHandler - resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiErrorResponseFactory.failure("NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(EmployeeCoreForbiddenException.class)
    public ResponseEntity<ApiResponse<Void>> handleForbidden(final EmployeeCoreForbiddenException ex) {
        log.warn("EmployeeCore EmployeeCoreGlobalExceptionHandler - forbidden: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ApiErrorResponseFactory.failure("FORBIDDEN", ex.getMessage()));
    }

    @ExceptionHandler(EmployeeCoreUnauthorizedException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorized(final EmployeeCoreUnauthorizedException ex) {
        log.warn("EmployeeCore EmployeeCoreGlobalExceptionHandler - unauthorized: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ApiErrorResponseFactory.failure("UNAUTHORIZED", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(final MethodArgumentNotValidException ex) {
        log.warn("EmployeeCore EmployeeCoreGlobalExceptionHandler - request validation failed: {}", ex.getMessage());
        return ResponseEntity.badRequest()
            .body(ApiErrorResponseFactory.validation(ex, "Validation failed."));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(final ConstraintViolationException ex) {
        log.warn("EmployeeCore EmployeeCoreGlobalExceptionHandler - constraint violation: {}", ex.getMessage());
        return ResponseEntity.badRequest()
            .body(ApiErrorResponseFactory.validation(ex, "Validation failed."));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotReadable(final HttpMessageNotReadableException ex) {
        log.warn("EmployeeCore EmployeeCoreGlobalExceptionHandler - malformed payload: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(ApiResponse.failure("MALFORMED_JSON", "Invalid request payload."));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(final IllegalArgumentException ex) {
        log.warn("EmployeeCore EmployeeCoreGlobalExceptionHandler - illegal argument: {}", ex.getMessage());
        return ResponseEntity.badRequest()
            .body(ApiErrorResponseFactory.failure("VALIDATION_FAILED", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnhandled(final Exception ex) {
        log.error("EmployeeCore EmployeeCoreGlobalExceptionHandler - unhandled exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiErrorResponseFactory.failure("INTERNAL_ERROR", "Internal server error."));
    }
}
