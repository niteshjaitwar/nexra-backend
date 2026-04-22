package com.nexra.hrms.nexra.modules.hrms.expense.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ExpenseDecisionRequest(
    @NotBlank @Size(max = 60) String tenantCode,
    @Size(max = 500) String comment
) {
}

