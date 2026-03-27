package com.nexra.hrms.nexra.modules.hrms.leave.model;

import java.math.BigDecimal;
import java.time.Instant;

public record LeaveBalance(
    String balanceId,
    String tenantCode,
    String employeeId,
    String leaveTypeCode,
    BigDecimal openingBalance,
    BigDecimal accruedBalance,
    BigDecimal usedBalance,
    BigDecimal adjustedBalance,
    BigDecimal availableBalance,
    Instant updatedAt,
    String updatedBy
) {
}

