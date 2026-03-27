package com.nexra.hrms.nexra.modules.hrms.leave.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record LeaveTypeUpsertRequest(
    @NotBlank @Size(max = 60) String tenantCode,
    @NotBlank @Size(max = 40) String code,
    @NotBlank @Size(max = 120) String name,
    @NotNull Boolean paid,
    @NotNull @DecimalMin("0.00") BigDecimal defaultAnnualQuota,
    Boolean active
) {
}

