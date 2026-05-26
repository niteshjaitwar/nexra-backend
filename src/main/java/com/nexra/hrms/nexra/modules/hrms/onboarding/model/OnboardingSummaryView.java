package com.nexra.hrms.nexra.modules.hrms.onboarding.model;

public record OnboardingSummaryView(
    String tenantCode,
    long totalPlans,
    long activePlans,
    long totalTasks,
    long completedTasks
) {
}
