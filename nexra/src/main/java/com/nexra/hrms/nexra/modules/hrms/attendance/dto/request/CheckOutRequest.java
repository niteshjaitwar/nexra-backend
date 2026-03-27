package com.nexra.hrms.nexra.modules.hrms.attendance.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record CheckOutRequest(
    @NotBlank @Size(max = 60) String tenantCode,
    @NotBlank @Size(max = 36) String employeeId,
    @NotNull LocalDate workDate,
    @Size(max = 500) String notes
) {
}

