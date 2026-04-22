package com.nexra.hrms.nexra.modules.hrms.onboarding.dto.request; import jakarta.validation.constraints.*; public record OnboardingTaskCompleteRequest(@NotBlank @Size(max=64) String tenantCode) {}

