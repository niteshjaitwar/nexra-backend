package com.nexra.hrms.nexra.modules.hrms.onboarding.exception;

import com.nexra.hrms.nexra.modules.hrms.onboarding.dto.ApiResponse;
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
 * Centralized exception handling for onboarding APIs.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@RestControllerAdvice(basePackages = "com.nexra.hrms.nexra.modules.hrms.onboarding")
@Slf4j
public class OnboardingGlobalExceptionHandler {

    @ExceptionHandler(OnboardingBusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(final OnboardingBusinessException ex) {
        log.warn("Onboarding OnboardingGlobalExceptionHandler - business error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.failure(ex.getMessage()));
    }

    @ExceptionHandler(OnboardingResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(final OnboardingResourceNotFoundException ex) {
        log.warn("Onboarding OnboardingGlobalExceptionHandler - resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.failure(ex.getMessage()));
    }

    @ExceptionHandler(OnboardingForbiddenException.class)
    public ResponseEntity<ApiResponse<Void>> handleForbidden(final OnboardingForbiddenException ex) {
        log.warn("Onboarding OnboardingGlobalExceptionHandler - forbidden: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.failure(ex.getMessage()));
    }

    @ExceptionHandler(OnboardingUnauthorizedException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorized(final OnboardingUnauthorizedException ex) {
        log.warn("Onboarding OnboardingGlobalExceptionHandler - unauthorized: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.failure(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleValidation(final MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err -> fieldErrors.put(err.getField(), err.getDefaultMessage()));
        return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Validation failed.", Map.of(
            "errors", fieldErrors,
            "timestamp", Instant.now()
        )));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleConstraintViolation(final ConstraintViolationException ex) {
        return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Validation failed.", Map.of(
            "details", ex.getMessage(),
            "timestamp", Instant.now()
        )));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleNotReadable(final HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Invalid request payload.", Map.of(
            "timestamp", Instant.now()
        )));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(final IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ApiResponse.failure(ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnhandled(final Exception ex) {
        log.error("Onboarding OnboardingGlobalExceptionHandler - unhandled exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.failure("Internal server error."));
    }
}
