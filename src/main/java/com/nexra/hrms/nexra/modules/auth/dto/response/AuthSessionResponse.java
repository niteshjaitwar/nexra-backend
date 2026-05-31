package com.nexra.hrms.nexra.modules.auth.dto.response;

import java.time.Instant;
import java.util.UUID;

public record AuthSessionResponse(
    UUID id,
    Instant createdAt,
    Instant expiresAt,
    boolean current
) {
}
