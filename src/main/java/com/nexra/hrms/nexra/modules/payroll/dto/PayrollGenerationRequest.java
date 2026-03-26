package com.nexra.hrms.nexra.modules.payroll.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public record PayrollGenerationRequest(
    @NotBlank String employeeId,
    @NotBlank String employeeCode,
    @NotBlank String employeeName,
    @NotBlank String tenantCode,
    @NotBlank String department,
    @NotBlank String designation,
    @NotBlank String payPeriod,
    @NotNull @DecimalMin(value = "0.00", inclusive = false) BigDecimal basicSalary,
    @Valid List<PayrollLineItemRequest> allowances,
    @Valid List<PayrollLineItemRequest> deductions,
    @DecimalMin(value = "0.00", inclusive = true) BigDecimal taxPercent,
    @DecimalMin(value = "0.00", inclusive = true) BigDecimal providentFundPercent,
    String currency
) {
}
