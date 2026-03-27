package com.nexra.hrms.nexra.modules.hrms.timesheet.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TimesheetDecisionRequest(
    @NotBlank @Size(max = 60) String tenantCode,
    @Size(max = 500) String comment
) {
}

