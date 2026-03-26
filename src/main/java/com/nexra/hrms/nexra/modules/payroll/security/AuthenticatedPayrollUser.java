package com.nexra.hrms.nexra.modules.payroll.security;

import java.util.Set;
import java.util.UUID;

public record AuthenticatedPayrollUser(
    UUID userId,
    String email,
    String tenantCode,
    Set<String> roles
) {
}
