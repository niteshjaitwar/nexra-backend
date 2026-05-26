package com.nexra.hrms.nexra.modules.hrms.recruitment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CandidateStageChangeRequest(
    @NotBlank @Size(max = 64) String tenantCode,
    @NotBlank @Size(max = 30) String stage,
    @Size(max = 500) String comment
) {
}
