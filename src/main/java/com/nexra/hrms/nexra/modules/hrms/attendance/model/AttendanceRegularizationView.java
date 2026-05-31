package com.nexra.hrms.nexra.modules.hrms.attendance.model;

import java.time.Instant;
import java.time.LocalDate;

public record AttendanceRegularizationView(
    String id,
    String tenantCode,
    String employeeId,
    LocalDate workDate,
    String reason,
    Instant requestedCheckInAt,
    Instant requestedCheckOutAt,
    String status,
    String approverEmail,
    String decisionComment
) {
}
