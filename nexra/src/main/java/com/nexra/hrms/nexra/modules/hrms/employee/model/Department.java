package com.nexra.hrms.nexra.modules.hrms.employee.model;

import java.time.Instant;

public record Department(
    String departmentId,
    String tenantCode,
    String code,
    String name,
    String managerEmployeeId,
    boolean active,
    Instant updatedAt,
    String updatedBy
) {
}
