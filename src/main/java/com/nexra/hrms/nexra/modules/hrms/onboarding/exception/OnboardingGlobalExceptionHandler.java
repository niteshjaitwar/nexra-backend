package com.nexra.hrms.nexra.modules.hrms.onboarding.exception;

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
 * Centralized exception handling for onboarding APIs.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 100)
@RestControllerAdvice(basePackages = "com.nexra.hrms.nexra.modules.hrms.onboarding")
@Slf4j
public class OnboardingGlobalExceptionHandler {

    @ExceptionHandler(OnboardingBusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(final OnboardingBusinessException ex) {
        log.warn("Onboarding OnboardingGlobalExceptionHandler - business error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ApiErrorResponseFactory.failure("BUSINESS_RULE_VIOLATION", ex.getMessage()));
    }

    @ExceptionHandler(OnboardingResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(final OnboardingResourceNotFoundException ex) {
        log.warn("Onboarding OnboardingGlobalExceptionHandler - resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiErrorResponseFactory.failure("NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(OnboardingForbiddenException.class)
    public ResponseEntity<ApiResponse<Void>> handleForbidden(final OnboardingForbiddenException ex) {
        log.warn("Onboarding OnboardingGlobalExceptionHandler - forbidden: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ApiErrorResponseFactory.failure("FORBIDDEN", ex.getMessage()));
    }

    @ExceptionHandler(OnboardingUnauthorizedException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorized(final OnboardingUnauthorizedException ex) {
        log.warn("Onboarding OnboardingGlobalExceptionHandler - unauthorized: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ApiErrorResponseFactory.failure("UNAUTHORIZED", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(final MethodArgumentNotValidException ex) {
        return ResponseEntity.badRequest()
            .body(ApiErrorResponseFactory.validation(ex, "Validation failed."));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(final ConstraintViolationException ex) {
        return ResponseEntity.badRequest()
            .body(ApiErrorResponseFactory.validation(ex, "Validation failed."));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotReadable(final HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body(ApiResponse.failure("MALFORMED_JSON", "Invalid request payload."));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(final IllegalArgumentException ex) {
        return ResponseEntity.badRequest()
            .body(ApiErrorResponseFactory.failure("VALIDATION_FAILED", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnhandled(final Exception ex) {
        log.error("Onboarding OnboardingGlobalExceptionHandler - unhandled exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiErrorResponseFactory.failure("INTERNAL_ERROR", "Internal server error."));
    }
}
