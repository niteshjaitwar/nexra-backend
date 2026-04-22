package com.nexra.hrms.nexra.modules.hrms.timesheet.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record TimesheetEntryView(
    String entryId, String tenantCode, String employeeId, LocalDate workDate, String projectCode, String taskName,
    BigDecimal hours, boolean billable, String status, String approverUserId, String approverEmail, String approvalComment,
    String notes, Instant createdAt, Instant updatedAt
) {
}

