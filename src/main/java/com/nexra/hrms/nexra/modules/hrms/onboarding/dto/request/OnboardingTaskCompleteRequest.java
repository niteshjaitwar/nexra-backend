package com.nexra.hrms.nexra.modules.hrms.onboarding.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OnboardingTaskCompleteRequest(@NotBlank @Size(max = 64) String tenantCode) {
}
