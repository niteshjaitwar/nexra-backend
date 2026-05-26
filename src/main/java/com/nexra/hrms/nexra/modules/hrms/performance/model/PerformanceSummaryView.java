package com.nexra.hrms.nexra.modules.hrms.performance.model;

public record PerformanceSummaryView(
    String tenantCode,
    long totalGoals,
    long activeGoals,
    long totalReviews,
    long completedReviews
) {
}
