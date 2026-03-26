package com.nexra.hrms.nexra.modules.payroll.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.List;

public record ProfilePayrollGenerationRequest(
    @NotBlank String tenantCode,
    @NotBlank String employeeId,
    @NotBlank String payPeriod,
    @Valid List<PayrollLineItemRequest> allowances,
    @Valid List<PayrollLineItemRequest> deductions,
    @DecimalMin(value = "0.00", inclusive = true) BigDecimal taxPercentOverride,
    @DecimalMin(value = "0.00", inclusive = true) BigDecimal providentFundPercentOverride,
    String currencyOverride
) {
}
