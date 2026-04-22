package com.nexra.hrms.nexra.modules.hrms.expense.exception;

import com.nexra.hrms.nexra.common.api.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Centralized exception handling for expense APIs.
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 100)
@RestControllerAdvice(basePackages = "com.nexra.hrms.nexra.modules.hrms.expense")
@Slf4j
public class ExpenseGlobalExceptionHandler {

    @ExceptionHandler(ExpenseBusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(
        final ExpenseBusinessException ex,
        final HttpServletRequest request
    ) {
        log.warn("ExpenseGlobalExceptionHandler - business error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.failure(ex.getMessage()));
    }

    @ExceptionHandler(ExpenseResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(
        final ExpenseResourceNotFoundException ex,
        final HttpServletRequest request
    ) {
        log.warn("ExpenseGlobalExceptionHandler - resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.failure(ex.getMessage()));
    }

    @ExceptionHandler(ExpenseForbiddenException.class)
    public ResponseEntity<ApiResponse<Void>> handleForbidden(
        final ExpenseForbiddenException ex,
        final HttpServletRequest request
    ) {
        log.warn("ExpenseGlobalExceptionHandler - forbidden: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.failure(ex.getMessage()));
    }

    @ExceptionHandler(ExpenseUnauthorizedException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorized(
        final ExpenseUnauthorizedException ex,
        final HttpServletRequest request
    ) {
        log.warn("ExpenseGlobalExceptionHandler - unauthorized: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.failure(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(
        final MethodArgumentNotValidException ex,
        final HttpServletRequest request
    ) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(error -> error.getField() + " " + error.getDefaultMessage())
            .orElse("Validation failed.");
        log.warn("ExpenseGlobalExceptionHandler - validation failed: {}", message);
        return ResponseEntity.badRequest().body(ApiResponse.failure(message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnhandled(
        final Exception ex,
        final HttpServletRequest request
    ) {
        log.error("ExpenseGlobalExceptionHandler - unhandled exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.failure("Unexpected server error."));
    }
}

