package com.nexra.hrms.nexra.modules.hrms.leave.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record LeaveRequestCreateRequest(
    @NotBlank @Size(max = 60) String tenantCode,
    @NotBlank @Size(max = 36) String employeeId,
    @NotBlank @Size(max = 40) String leaveTypeCode,
    @NotNull LocalDate startDate,
    @NotNull LocalDate endDate,
    @Size(max = 500) String reason
) {
}

