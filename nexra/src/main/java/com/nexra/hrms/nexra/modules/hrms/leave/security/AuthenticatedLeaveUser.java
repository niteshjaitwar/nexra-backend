package com.nexra.hrms.nexra.modules.hrms.leave.security;

import java.util.Set;
import java.util.UUID;

public record AuthenticatedLeaveUser(
    UUID userId,
    String email,
    String tenantCode,
    Set<String> roles
) {
}

