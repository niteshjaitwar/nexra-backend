package com.nexra.hrms.nexra.modules.hrms.leave.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record LeaveRequestView(
    String requestId,
    String tenantCode,
    String employeeId,
    String leaveTypeCode,
    LocalDate startDate,
    LocalDate endDate,
    BigDecimal totalDays,
    String reason,
    String status,
    String approverUserId,
    String approverEmail,
    String decisionComment,
    Instant updatedAt,
    String updatedBy
) {
}

