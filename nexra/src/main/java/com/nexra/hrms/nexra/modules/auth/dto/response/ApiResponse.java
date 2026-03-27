package com.nexra.hrms.nexra.modules.auth.dto.response;

import java.time.Instant;

/**
 * Represents data contract for ApiResponse.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
public record ApiResponse<T>(
    boolean success,
    String message,
    T data,
    Instant timestamp
) {

    public static <T> ApiResponse<T> success(final String message, final T data) {
        return new ApiResponse<>(true, message, data, Instant.now());
    }

    public static <T> ApiResponse<T> failure(final String message, final T data) {
        return new ApiResponse<>(false, message, data, Instant.now());
    }
}
