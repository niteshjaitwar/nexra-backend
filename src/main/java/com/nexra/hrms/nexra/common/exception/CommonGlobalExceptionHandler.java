package com.nexra.hrms.nexra.common.exception;

import com.nexra.hrms.nexra.common.api.ApiError;
import com.nexra.hrms.nexra.common.api.ApiResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.List;

/**
 * Last resort global exception handler. Runs at LOWEST_PRECEDENCE so per
 * module ControllerAdvice classes keep their existing behaviour and only the
 * unmapped exceptions fall through to this handler. Produces a canonical
 * ApiResponse envelope for every unhandled error so clients always see a
 * predictable shape. Never leaks stack traces to the wire.
 *
 * @author niteshjaitwar
 */
@Slf4j
@Order(Ordered.LOWEST_PRECEDENCE)
@RestControllerAdvice
public class CommonGlobalExceptionHandler {

    /**
     * Maps every Nexra domain exception to its carried HTTP status and error code.
     *
     * @param ex the domain exception instance.
     * @return ResponseEntity with the canonical envelope.
     */
    @ExceptionHandler(NexraException.class)
    public ResponseEntity<ApiResponse<Void>> handleNexra(final NexraException ex) {
        log.warn("CommonGlobalExceptionHandler - handleNexra() - code={}, message={}", ex.getErrorCode(), ex.getMessage());
        return ResponseEntity.status(ex.getStatus())
                .body(ApiResponse.failure(ex.getErrorCode(), ex.getMessage()));
    }

    /**
     * Maps bean validation failures to HTTP 400 with structured field errors.
     *
     * @param ex the validation exception.
     * @return ResponseEntity with a VALIDATION_FAILED envelope.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(final MethodArgumentNotValidException ex) {
        final List<ApiError> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toApiError)
                .toList();
        log.warn("CommonGlobalExceptionHandler - handleValidation() - count={}", errors.size());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.failure("VALIDATION_FAILED", "Request payload failed validation.", errors));
    }

    /**
     * Maps jakarta constraint violations to HTTP 400 with structured errors.
     *
     * @param ex the constraint exception.
     * @return ResponseEntity with a VALIDATION_FAILED envelope.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraint(final ConstraintViolationException ex) {
        final List<ApiError> errors = ex.getConstraintViolations().stream()
                .map(this::toApiError)
                .toList();
        log.warn("CommonGlobalExceptionHandler - handleConstraint() - count={}", errors.size());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.failure("VALIDATION_FAILED", "Request failed constraint validation.", errors));
    }

    /**
     * Maps malformed JSON payloads to HTTP 400.
     *
     * @param ex the Jackson deserialization exception.
     * @return ResponseEntity with a MALFORMED_JSON envelope.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnreadable(final HttpMessageNotReadableException ex) {
        log.warn("CommonGlobalExceptionHandler - handleUnreadable() - reason={}", ex.getMostSpecificCause().getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.failure("MALFORMED_JSON", "Request body could not be parsed."));
    }

    /**
     * Maps path or query parameter type mismatches to HTTP 400.
     *
     * @param ex the Spring type mismatch exception.
     * @return ResponseEntity with a TYPE_MISMATCH envelope.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(final MethodArgumentTypeMismatchException ex) {
        log.warn("CommonGlobalExceptionHandler - handleTypeMismatch() - param={}, value={}", ex.getName(), ex.getValue());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.failure("TYPE_MISMATCH", "Parameter '" + ex.getName() + "' has an invalid value."));
    }

    /**
     * Maps optimistic locking conflicts to HTTP 409 so the client can retry.
     *
     * @param ex the optimistic locking failure.
     * @return ResponseEntity with a CONFLICT envelope.
     */
    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ApiResponse<Void>> handleOptimistic(final OptimisticLockingFailureException ex) {
        log.warn("CommonGlobalExceptionHandler - handleOptimistic() - reason={}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.failure("CONCURRENT_UPDATE", "Resource was modified by another request, please retry."));
    }

    /**
     * Maps database integrity errors (duplicate keys, FK violations) to HTTP 409.
     *
     * @param ex the data integrity exception.
     * @return ResponseEntity with a CONFLICT envelope.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleIntegrity(final DataIntegrityViolationException ex) {
        log.warn("CommonGlobalExceptionHandler - handleIntegrity() - reason={}", ex.getMostSpecificCause().getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.failure("DATA_INTEGRITY", "Operation violates a data integrity constraint."));
    }

    /**
     * Maps authentication failures to HTTP 401.
     *
     * @param ex the authentication exception.
     * @return ResponseEntity with an UNAUTHORIZED envelope.
     */
    @ExceptionHandler({AuthenticationException.class, BadCredentialsException.class})
    public ResponseEntity<ApiResponse<Void>> handleAuth(final AuthenticationException ex) {
        log.warn("CommonGlobalExceptionHandler - handleAuth() - reason={}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.failure("UNAUTHORIZED", "Authentication required."));
    }

    /**
     * Maps Spring Security access denied to HTTP 403.
     *
     * @param ex the access denied exception.
     * @return ResponseEntity with a FORBIDDEN envelope.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(final AccessDeniedException ex) {
        log.warn("CommonGlobalExceptionHandler - handleAccessDenied() - reason={}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.failure("FORBIDDEN", "You are not allowed to perform this action."));
    }

    /**
     * Maps missing route matches to HTTP 404.
     *
     * @param ex the no handler found exception.
     * @return ResponseEntity with a NOT_FOUND envelope.
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoHandler(final NoHandlerFoundException ex) {
        log.warn("CommonGlobalExceptionHandler - handleNoHandler() - path={}", ex.getRequestURL());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.failure("NOT_FOUND", "Requested resource was not found."));
    }

    /**
     * Catch all for anything unmapped. Never leaks internals and always
     * returns HTTP 500 with a generic envelope while logging the cause.
     *
     * @param ex the unmapped exception.
     * @return ResponseEntity with an INTERNAL_ERROR envelope.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleFallback(final Exception ex) {
        log.error("CommonGlobalExceptionHandler - handleFallback() - type={}, message={}", ex.getClass().getSimpleName(), ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.failure("INTERNAL_ERROR", "An unexpected error occurred. Please try again later."));
    }

    /**
     * Transforms a Spring FieldError into an ApiError.
     *
     * @param fieldError the Spring field error.
     * @return mapped ApiError.
     */
    private ApiError toApiError(final FieldError fieldError) {
        final String message = fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage() : "Invalid value.";
        return ApiError.ofField(fieldError.getField(), fieldError.getCode() != null ? fieldError.getCode() : "INVALID", message);
    }

    /**
     * Transforms a jakarta constraint violation into an ApiError.
     *
     * @param violation the constraint violation.
     * @return mapped ApiError.
     */
    private ApiError toApiError(final ConstraintViolation<?> violation) {
        return ApiError.ofField(violation.getPropertyPath().toString(), "INVALID", violation.getMessage());
    }
}
