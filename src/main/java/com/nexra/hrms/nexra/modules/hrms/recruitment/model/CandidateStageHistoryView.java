package com.nexra.hrms.nexra.modules.hrms.recruitment.model;

public record CandidateStageHistoryView(
    String historyId,
    String tenantCode,
    String candidateId,
    String fromStage,
    String toStage,
    String comment
) {
}
