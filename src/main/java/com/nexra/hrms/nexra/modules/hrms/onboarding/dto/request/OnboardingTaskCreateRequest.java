package com.nexra.hrms.nexra.modules.hrms.onboarding.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OnboardingTaskCreateRequest(
    @NotBlank @Size(max = 64) String tenantCode,
    @NotBlank @Size(max = 200) String taskName,
    @Size(max = 80) String ownerTeam
) {
}
