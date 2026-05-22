package com.nexra.hrms.nexra.modules.hrms.recruitment.exception;

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
 * Maps recruitment module failures to stable JSON API responses.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE + 100)
@RestControllerAdvice(basePackages = "com.nexra.hrms.nexra.modules.hrms.recruitment")
public class RecruitmentGlobalExceptionHandler {

    @ExceptionHandler(RecruitmentUnauthorizedException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorized(
        final RecruitmentUnauthorizedException exception,
        final HttpServletRequest request
    ) {
        return build(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", exception.getMessage());
    }

    @ExceptionHandler(RecruitmentForbiddenException.class)
    public ResponseEntity<ApiResponse<Void>> handleForbidden(
        final RecruitmentForbiddenException exception,
        final HttpServletRequest request
    ) {
        return build(HttpStatus.FORBIDDEN, "FORBIDDEN", exception.getMessage());
    }

    @ExceptionHandler(RecruitmentResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(
        final RecruitmentResourceNotFoundException exception,
        final HttpServletRequest request
    ) {
        return build(HttpStatus.NOT_FOUND, "NOT_FOUND", exception.getMessage());
    }

    @ExceptionHandler(RecruitmentBusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(
        final RecruitmentBusinessException exception,
        final HttpServletRequest request
    ) {
        return build(HttpStatus.CONFLICT, "BUSINESS_RULE_VIOLATION", exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(
        final MethodArgumentNotValidException exception,
        final HttpServletRequest request
    ) {
        final String message = exception.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(error -> error.getField() + " " + error.getDefaultMessage())
            .orElse("Validation failed.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiErrorResponseFactory.validation(exception, message));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleMalformedPayload(
        final HttpMessageNotReadableException exception,
        final HttpServletRequest request
    ) {
        return build(HttpStatus.BAD_REQUEST, "MALFORMED_JSON", "Invalid request payload.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(
        final Exception exception,
        final HttpServletRequest request
    ) {
        log.error("Unhandled recruitment module exception", exception);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Unexpected server error.");
    }

    private ResponseEntity<ApiResponse<Void>> build(
        final HttpStatus status,
        final String code,
        final String message
    ) {
        return ResponseEntity.status(status).body(ApiErrorResponseFactory.failure(code, message));
    }
}
