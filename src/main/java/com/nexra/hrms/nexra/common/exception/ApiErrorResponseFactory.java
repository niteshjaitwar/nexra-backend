package com.nexra.hrms.nexra.common.exception;

import com.nexra.hrms.nexra.common.api.ApiError;
import com.nexra.hrms.nexra.common.api.ApiResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

/**
 * Builds canonical API failure envelopes for module-level exception handlers.
 *
 * <p>This keeps per-module {@code @RestControllerAdvice} classes aligned with
 * the shared {@link ApiResponse}/{@link ApiError} contract without forcing each
 * module to duplicate field-error mapping logic.</p>
 *
 * @author niteshjaitwar
 */
public final class ApiErrorResponseFactory {

    private ApiErrorResponseFactory() {
    }

    public static ApiResponse<Void> failure(final String code, final String message) {
        return ApiResponse.failure(code, message);
    }

    public static ApiResponse<Void> validation(
        final MethodArgumentNotValidException exception,
        final String message
    ) {
        final List<ApiError> errors = exception.getBindingResult().getFieldErrors().stream()
            .map(ApiErrorResponseFactory::toApiError)
            .toList();
        return ApiResponse.failure("VALIDATION_FAILED", message, errors);
    }

    public static ApiResponse<Void> validation(
        final ConstraintViolationException exception,
        final String message
    ) {
        final List<ApiError> errors = exception.getConstraintViolations().stream()
            .map(ApiErrorResponseFactory::toApiError)
            .toList();
        return ApiResponse.failure("VALIDATION_FAILED", message, errors);
    }

    private static ApiError toApiError(final FieldError fieldError) {
        final String code = fieldError.getCode() != null ? fieldError.getCode() : "INVALID";
        final String message = fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage() : "Invalid value.";
        return ApiError.ofField(fieldError.getField(), code, message);
    }

    private static ApiError toApiError(final ConstraintViolation<?> violation) {
        return ApiError.ofField(violation.getPropertyPath().toString(), "INVALID", violation.getMessage());
    }
}
