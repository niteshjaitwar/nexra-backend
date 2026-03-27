package com.nexra.hrms.nexra.modules.hrms.timesheet.security;

import java.util.Set;
import java.util.UUID;

public record AuthenticatedTimesheetUser(UUID userId, String email, String tenantCode, Set<String> roles) {
}

