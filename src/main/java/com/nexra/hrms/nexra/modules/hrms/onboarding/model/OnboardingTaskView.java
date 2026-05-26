package com.nexra.hrms.nexra.modules.hrms.onboarding.model;

public record OnboardingTaskView(
    String taskId,
    String tenantCode,
    String planId,
    String taskName,
    String ownerTeam,
    String status
) {
}
