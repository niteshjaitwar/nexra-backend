package com.nexra.hrms.nexra.common.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.slf4j.MDC;

/**
 * Canonical API envelope returned by every REST endpoint across the Nexra
 * modular monolith. Carries a success flag, a human readable message, a
 * machine readable code, timestamp, optional error details and an optional
 * pagination block. Modules MUST reuse this type and MUST NOT declare their
 * own duplicate response record.
 *
 * @param success   overall success flag.
 * @param code      stable machine readable status code such as OK, CREATED or VALIDATION_FAILED.
 * @param message   human readable description safe to surface to end users.
 * @param data      response payload, may be null for errors or empty success.
 * @param errors    optional list of structured errors when success is false.
 * @param meta      optional arbitrary metadata bag (e.g. pagination, correlationId).
 * @param timestamp UTC instant the envelope was produced on the server.
 * @param <T>       payload type.
 * @author niteshjaitwar
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        String code,
        String message,
        T data,
        List<ApiError> errors,
        Map<String, Object> meta,
        Instant timestamp) {

    /**
     * Builds a successful envelope with default OK code.
     *
     * @param data    payload.
     * @param message user facing message.
     * @param <T>     payload type.
     * @return immutable envelope.
     */
    public static <T> ApiResponse<T> ok(final T data, final String message) {
        return new ApiResponse<>(true, "OK", message, data, null, null, Instant.now());
    }

    /**
     * Legacy-compatible alias for success responses used by existing modules.
     * Prefer {@link #ok(Object, String)} for new code.
     *
     * @param message user facing message.
     * @param data payload.
     * @param <T> payload type.
     * @return immutable success envelope.
     */
    public static <T> ApiResponse<T> success(final String message, final T data) {
        return ok(data, message);
    }

    /**
     * Builds a successful envelope for newly created resources.
     *
     * @param data    payload.
     * @param message user facing message.
     * @param <T>     payload type.
     * @return immutable envelope tagged with CREATED.
     */
    public static <T> ApiResponse<T> created(final T data, final String message) {
        return new ApiResponse<>(true, "CREATED", message, data, null, null, Instant.now());
    }

    /**
     * Builds a successful envelope with no payload.
     *
     * @param message user facing message.
     * @return immutable envelope tagged with OK and null data.
     */
    public static ApiResponse<Void> empty(final String message) {
        return new ApiResponse<>(true, "OK", message, null, null, null, Instant.now());
    }

    /**
     * Builds a failure envelope with a single error.
     *
     * @param code    machine readable error code.
     * @param message user facing description.
     * @return immutable envelope with success=false.
     */
    public static ApiResponse<Void> failure(final String code, final String message) {
        return new ApiResponse<>(false, code, message, null, null, requestMeta(), Instant.now());
    }

    /**
     * Legacy-compatible failure helper that defaults the machine code to ERROR.
     * Prefer {@link #failure(String, String)} for new code.
     *
     * @param message user facing description.
     * @return immutable failure envelope.
     */
    public static ApiResponse<Void> failure(final String message) {
        return failure("ERROR", message);
    }

    /**
     * Builds a failure envelope carrying field level errors.
     *
     * @param code    machine readable error code.
     * @param message user facing description.
     * @param errors  list of structured errors.
     * @return immutable envelope with success=false and errors populated.
     */
    public static ApiResponse<Void> failure(final String code, final String message, final List<ApiError> errors) {
        return new ApiResponse<>(false, code, message, null, errors, requestMeta(), Instant.now());
    }

    /**
     * Attaches arbitrary metadata such as correlation id or pagination block.
     *
     * @param key   metadata key.
     * @param value metadata value.
     * @return new envelope instance with metadata merged.
     */
    public ApiResponse<T> withMeta(final String key, final Object value) {
        final Map<String, Object> merged = new java.util.LinkedHashMap<>(meta == null ? Map.of() : meta);
        merged.put(key, value);
        return new ApiResponse<>(success, code, message, data, errors, merged, timestamp);
    }

    private static Map<String, Object> requestMeta() {
        final String requestId = MDC.get("requestId");
        return requestId == null || requestId.isBlank() ? null : Map.of("requestId", requestId);
    }
}
