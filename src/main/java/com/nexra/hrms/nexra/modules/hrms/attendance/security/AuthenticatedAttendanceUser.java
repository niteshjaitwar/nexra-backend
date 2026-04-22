package com.nexra.hrms.nexra.modules.hrms.attendance.security;

import java.util.Set;
import java.util.UUID;

public record AuthenticatedAttendanceUser(UUID userId, String email, String tenantCode, Set<String> roles) {
}

