package com.nexra.hrms.nexra.modules.hrms.timesheet.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record TimesheetEntryCreateRequest(
    @NotBlank @Size(max = 60) String tenantCode,
    @NotBlank @Size(max = 36) String employeeId,
    @NotNull LocalDate workDate,
    @NotBlank @Size(max = 60) String projectCode,
    @NotBlank @Size(max = 160) String taskName,
    @NotNull @DecimalMin("0.25") @DecimalMax("24.00") BigDecimal hours,
    @NotNull Boolean billable,
    @Size(max = 500) String notes
) {
}

