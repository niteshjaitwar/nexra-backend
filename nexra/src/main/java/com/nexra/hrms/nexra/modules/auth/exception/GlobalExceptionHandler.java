package com.nexra.hrms.nexra.modules.auth.exception;

import com.nexra.hrms.nexra.modules.auth.dto.response.ApiResponse;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Centralized REST exception mapper for converting domain and framework errors into API responses.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@Slf4j
@RestControllerAdvice(basePackages = "com.nexra.hrms.nexra.modules.auth")
public class GlobalExceptionHandler {

    /**
     * Maps bean validation failures to HTTP 400 responses.
     *
     * @param exception validation exception
     * @return standardized failure response
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<String>> handleValidationException(final MethodArgumentNotValidException exception) {
        String errors = exception.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining(", "));
        log.error("GlobalExceptionHandler() - handleValidationException() - Validation failed: {}", errors, exception);
        return ResponseEntity.badRequest().body(ApiResponse.failure(errors, null));
    }

    /**
     * Maps malformed JSON payload errors to HTTP 400 responses.
     *
     * @param exception payload parsing exception
     * @return standardized failure response
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<String>> handleHttpMessageNotReadableException(final HttpMessageNotReadableException exception) {
        log.error("GlobalExceptionHandler() - handleHttpMessageNotReadableException() - Invalid request payload: {}", exception.getMessage(), exception);
        return ResponseEntity.badRequest().body(ApiResponse.failure("Invalid request payload.", null));
    }

    /**
     * Maps resource-not-found errors to HTTP 404 responses.
     *
     * @param exception resource-not-found exception
     * @return standardized failure response
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<String>> handleResourceNotFoundException(final ResourceNotFoundException exception) {
        log.error("GlobalExceptionHandler() - handleResourceNotFoundException() - Resource missing: {}", exception.getMessage(), exception);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.failure(exception.getMessage(), null));
    }

    /**
     * Maps unauthorized errors to HTTP 401 responses.
     *
     * @param exception unauthorized exception
     * @return standardized failure response
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<String>> handleUnauthorizedException(final UnauthorizedException exception) {
        log.error("GlobalExceptionHandler() - handleUnauthorizedException() - Unauthorized access: {}", exception.getMessage(), exception);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.failure(exception.getMessage(), null));
    }

    /**
     * Maps business rule violations to HTTP 409 responses.
     *
     * @param exception business exception
     * @return standardized failure response
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<String>> handleBusinessException(final BusinessException exception) {
        log.error("GlobalExceptionHandler() - handleBusinessException() - Business violation: {}", exception.getMessage(), exception);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.failure(exception.getMessage(), null));
    }

    /**
     * Maps request throttling errors to HTTP 429 responses.
     *
     * @param exception rate-limit exception
     * @return standardized failure response
     */
    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ApiResponse<String>> handleRateLimitExceededException(final RateLimitExceededException exception) {
        log.warn("GlobalExceptionHandler() - handleRateLimitExceededException() - Request throttled: {}", exception.getMessage());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(ApiResponse.failure(exception.getMessage(), null));
    }

    /**
     * Maps access denied errors to HTTP 403 responses.
     *
     * @param exception access denied exception
     * @return standardized failure response
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<String>> handleAccessDeniedException(final AccessDeniedException exception) {
        log.error("GlobalExceptionHandler() - handleAccessDeniedException() - Access denied: {}", exception.getMessage(), exception);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.failure("Access denied.", null));
    }

    /**
     * Maps unhandled errors to HTTP 500 responses.
     *
     * @param exception unexpected exception
     * @return standardized failure response
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleUnhandledException(final Exception exception) {
        log.error("GlobalExceptionHandler() - handleUnhandledException() - Unexpected error: {}", exception.getMessage(), exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.failure("An unexpected error occurred.", null));
    }
}
