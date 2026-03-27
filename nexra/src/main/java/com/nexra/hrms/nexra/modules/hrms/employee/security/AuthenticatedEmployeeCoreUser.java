package com.nexra.hrms.nexra.modules.hrms.employee.security;

import java.util.Set;
import java.util.UUID;

public record AuthenticatedEmployeeCoreUser(
    UUID userId,
    String email,
    String tenantCode,
    Set<String> roles
) {
}
