package com.nexra.hrms.nexra.modules.hrms.attendance.dto.request;

import com.nexra.hrms.nexra.modules.hrms.employee.validation.TenantCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.time.LocalDate;

public record AttendanceRegularizationSubmitRequest(
    @NotBlank @TenantCode String tenantCode,
    @NotBlank @Size(max = 36) String employeeId,
    @NotNull LocalDate workDate,
    @Size(max = 500) String reason,
    Instant requestedCheckInAt,
    Instant requestedCheckOutAt
) {
}
