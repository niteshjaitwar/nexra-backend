package com.nexra.hrms.nexra.modules.hrms.leave.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record LeaveBalanceAdjustRequest(
    @NotBlank @Size(max = 60) String tenantCode,
    @NotBlank @Size(max = 36) String employeeId,
    @NotBlank @Size(max = 40) String leaveTypeCode,
    @NotNull @DecimalMin("0.00") BigDecimal openingBalance,
    @NotNull @DecimalMin("0.00") BigDecimal accruedBalance,
    @NotNull @DecimalMin("0.00") BigDecimal adjustedBalance
) {
}

