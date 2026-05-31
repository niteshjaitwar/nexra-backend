package com.nexra.hrms.nexra.modules.operations.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record OpsApprovalDecisionRequest(
    @NotBlank @Pattern(regexp = "APPROVE|REJECT", message = "decision must be APPROVE or REJECT") String decision,
    @NotBlank @Size(max = 36) String approverUserId,
    @Size(max = 2000) String notes
) {
}
