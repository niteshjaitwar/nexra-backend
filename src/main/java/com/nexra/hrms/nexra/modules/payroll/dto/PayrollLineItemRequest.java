package com.nexra.hrms.nexra.modules.payroll.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public record PayrollLineItemRequest(
    @NotBlank String name,
    @DecimalMin(value = "0.00", inclusive = true) BigDecimal amount
) {
}
