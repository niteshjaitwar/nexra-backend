package com.nexra.hrms.nexra.modules.hrms.timesheet.model;

import java.time.Instant;

public record ProjectView(
    String projectId, String tenantCode, String projectCode, String projectName, String clientName,
    boolean billable, boolean active, Instant updatedAt, String updatedBy
) {
}

