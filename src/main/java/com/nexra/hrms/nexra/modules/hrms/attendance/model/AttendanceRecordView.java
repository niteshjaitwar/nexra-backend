package com.nexra.hrms.nexra.modules.hrms.attendance.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record AttendanceRecordView(
    String recordId,
    String tenantCode,
    String employeeId,
    LocalDate workDate,
    String shiftCode,
    Instant checkInAt,
    Instant checkOutAt,
    BigDecimal totalHours,
    String status,
    String notes,
    Instant updatedAt,
    String updatedBy
) {
}

