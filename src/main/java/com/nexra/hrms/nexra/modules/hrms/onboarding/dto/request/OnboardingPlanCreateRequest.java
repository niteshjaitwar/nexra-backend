package com.nexra.hrms.nexra.modules.hrms.onboarding.dto.request; import jakarta.validation.constraints.*; public record OnboardingPlanCreateRequest(@NotBlank @Size(max=64) String tenantCode,@NotBlank @Size(max=36) String employeeId,@NotBlank @Size(max=200) String planName) {}

