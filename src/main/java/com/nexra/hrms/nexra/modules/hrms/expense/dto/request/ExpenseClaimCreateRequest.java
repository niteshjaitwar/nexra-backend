package com.nexra.hrms.nexra.modules.hrms.expense.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

public record ExpenseClaimCreateRequest(
    @NotBlank @Size(max = 60) String tenantCode,
    @NotBlank @Size(max = 36) String employeeId,
    @NotNull LocalDate claimDate,
    @NotBlank @Size(max = 200) String title,
    @NotBlank @Size(max = 12) String currency,
    @NotEmpty List<@Valid ExpenseClaimItemRequest> items
) {
}

