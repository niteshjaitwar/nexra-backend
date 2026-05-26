package com.nexra.hrms.nexra.modules.hrms.recruitment.model;

public record CandidateView(
    String candidateId,
    String tenantCode,
    String jobId,
    String fullName,
    String email,
    String phone,
    String stage
) {
}
