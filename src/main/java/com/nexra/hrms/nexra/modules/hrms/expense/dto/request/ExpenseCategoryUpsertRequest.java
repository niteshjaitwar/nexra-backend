package com.nexra.hrms.nexra.modules.hrms.expense.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ExpenseCategoryUpsertRequest(
    @NotBlank @Size(max = 60) String tenantCode,
    @NotBlank @Size(max = 40) String code,
    @NotBlank @Size(max = 120) String name,
    @DecimalMin("0.00") BigDecimal maxAmountPerClaim,
    @NotNull Boolean requiresReceipt,
    Boolean active
) {
}

