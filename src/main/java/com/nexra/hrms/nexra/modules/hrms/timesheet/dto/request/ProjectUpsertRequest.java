package com.nexra.hrms.nexra.modules.hrms.timesheet.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProjectUpsertRequest(
    @NotBlank @Size(max = 60) String tenantCode,
    @NotBlank @Size(max = 60) String projectCode,
    @NotBlank @Size(max = 160) String projectName,
    @Size(max = 160) String clientName,
    @NotNull Boolean billable,
    Boolean active
) {
}

