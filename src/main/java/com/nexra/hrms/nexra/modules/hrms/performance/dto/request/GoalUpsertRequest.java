package com.nexra.hrms.nexra.modules.hrms.performance.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record GoalUpsertRequest(
    @NotBlank @Size(max = 64) String tenantCode,
    @Size(max = 36) String goalId,
    @NotBlank @Size(max = 36) String employeeId,
    @NotBlank @Size(max = 200) String title,
    @Size(max = 1000) String description,
    LocalDate targetDate,
    @NotBlank @Size(max = 30) String status
) {
}
