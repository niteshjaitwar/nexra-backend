package com.nexra.hrms.nexra.modules.auth.dto.response;

import java.util.Set;
import java.util.UUID;

/**
 * Authenticated session profile returned by {@code GET /api/v1/auth/me}.
 */
public record AuthMeResponse(
    UUID userId,
    String tenantCode,
    String email,
    String firstName,
    String lastName,
    boolean emailVerified,
    boolean mfaEnabled,
    Set<String> roles,
    Set<String> products
) {
}
