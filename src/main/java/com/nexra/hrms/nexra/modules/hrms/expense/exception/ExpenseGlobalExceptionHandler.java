package com.nexra.hrms.nexra.modules.hrms.expense.exception;

import com.nexra.hrms.nexra.common.api.ApiResponse;
import com.nexra.hrms.nexra.common.exception.ApiErrorResponseFactory;
import jakarta.servlet.http.HttpServletRequest;
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
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ApiErrorResponseFactory.failure("BUSINESS_RULE_VIOLATION", ex.getMessage()));
    }

    @ExceptionHandler(ExpenseResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(
        final ExpenseResourceNotFoundException ex,
        final HttpServletRequest request
    ) {
        log.warn("ExpenseGlobalExceptionHandler - resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiErrorResponseFactory.failure("NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(ExpenseForbiddenException.class)
    public ResponseEntity<ApiResponse<Void>> handleForbidden(
        final ExpenseForbiddenException ex,
        final HttpServletRequest request
    ) {
        log.warn("ExpenseGlobalExceptionHandler - forbidden: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ApiErrorResponseFactory.failure("FORBIDDEN", ex.getMessage()));
    }

    @ExceptionHandler(ExpenseUnauthorizedException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorized(
        final ExpenseUnauthorizedException ex,
        final HttpServletRequest request
    ) {
        log.warn("ExpenseGlobalExceptionHandler - unauthorized: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ApiErrorResponseFactory.failure("UNAUTHORIZED", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(
        final MethodArgumentNotValidException ex,
        final HttpServletRequest request
    ) {
        final String message = ex.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(error -> error.getField() + " " + error.getDefaultMessage())
            .orElse("Validation failed.");
        log.warn("ExpenseGlobalExceptionHandler - validation failed: {}", message);
        return ResponseEntity.badRequest()
            .body(ApiErrorResponseFactory.validation(ex, message));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleMalformedPayload(
        final HttpMessageNotReadableException ex,
        final HttpServletRequest request
    ) {
        log.warn("ExpenseGlobalExceptionHandler - malformed payload: {}", ex.getMessage());
        return ResponseEntity.badRequest()
            .body(ApiResponse.failure("MALFORMED_JSON", "Invalid request payload."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnhandled(
        final Exception ex,
        final HttpServletRequest request
    ) {
        log.error("ExpenseGlobalExceptionHandler - unhandled exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiErrorResponseFactory.failure("INTERNAL_ERROR", "Unexpected server error."));
    }
}

