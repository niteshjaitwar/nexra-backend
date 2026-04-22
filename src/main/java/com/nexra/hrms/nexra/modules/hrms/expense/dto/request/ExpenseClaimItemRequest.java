package com.nexra.hrms.nexra.modules.hrms.expense.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseClaimItemRequest(
    @NotNull LocalDate expenseDate,
    @NotBlank @Size(max = 40) String categoryCode,
    @NotBlank @Size(max = 300) String description,
    @NotNull @DecimalMin("0.01") BigDecimal amount,
    @Size(max = 200) String receiptReference
) {
}

