package com.nexra.hrms.nexra.modules.hrms.recruitment.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CandidateCreateRequest(
    @NotBlank @Size(max = 64) String tenantCode,
    @NotBlank @Size(max = 36) String jobId,
    @NotBlank @Size(max = 180) String fullName,
    @Email @Size(max = 180) String email,
    @Size(max = 40) String phone
) {
}
