package com.nexra.hrms.nexra.modules.hrms.leave.model;

import java.math.BigDecimal;
import java.time.Instant;

public record LeaveType(
    String leaveTypeId,
    String tenantCode,
    String code,
    String name,
    boolean paid,
    BigDecimal defaultAnnualQuota,
    boolean active,
    Instant updatedAt,
    String updatedBy
) {
}

