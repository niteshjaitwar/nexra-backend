package com.nexra.hrms.nexra.modules.hrms.leave.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LeaveDecisionRequest(
    @NotBlank @Size(max = 60) String tenantCode,
    @Size(max = 500) String decisionComment
) {
}

