package com.nexra.hrms.nexra.modules.operations.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OpsApprovalCreateRequest(
    @NotBlank @Size(max = 60) String referenceType,
    @NotBlank @Size(max = 36) String referenceId,
    @NotBlank @Size(max = 36) String requestedByUserId,
    @Size(max = 36) String approverUserId,
    @Size(max = 2000) String notes
) {
}
