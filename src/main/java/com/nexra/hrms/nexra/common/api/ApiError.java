package com.nexra.hrms.nexra.common.api;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Structured error payload attached to an ApiResponse when success is false.
 * Captures the offending field (for validation errors), a machine readable
 * error code and the user facing message.
 *
 * @param field   dotted path of the offending field, null for non field errors.
 * @param code    stable machine readable error code (e.g. VALIDATION_FAILED, NOT_FOUND).
 * @param message user facing description safe to surface to clients.
 * @author niteshjaitwar
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(String field, String code, String message) {

    /**
     * Builds a non field scoped error.
     *
     * @param code    machine readable error code.
     * @param message user facing description.
     * @return immutable error instance.
     */
    public static ApiError of(final String code, final String message) {
        return new ApiError(null, code, message);
    }

    /**
     * Builds a field scoped error for validation failures.
     *
     * @param field   dotted path of the offending field.
     * @param code    machine readable error code.
     * @param message user facing description.
     * @return immutable error instance.
     */
    public static ApiError ofField(final String field, final String code, final String message) {
        return new ApiError(field, code, message);
    }
}
