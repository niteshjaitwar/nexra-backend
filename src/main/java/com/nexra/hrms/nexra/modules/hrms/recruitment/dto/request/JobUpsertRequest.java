package com.nexra.hrms.nexra.modules.hrms.recruitment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record JobUpsertRequest(
    @NotBlank @Size(max = 64) String tenantCode,
    @Size(max = 36) String jobId,
    @NotBlank @Size(max = 200) String title,
    @Size(max = 120) String department,
    @Size(max = 120) String location,
    @NotBlank @Size(max = 30) String status
) {
}
