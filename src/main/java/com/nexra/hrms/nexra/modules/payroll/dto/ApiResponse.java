package com.nexra.hrms.nexra.modules.payroll.dto;

import java.time.Instant;

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

    public static <T> ApiResponse<T> failure(final String message) {
        return failure(message, null);
    }
}
