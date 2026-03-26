package com.nexra.hrms.nexra.modules.payroll.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record PayrollSlip(
    String slipId,
    String tenantCode,
    String employeeId,
    String employeeCode,
    String employeeName,
    String department,
    String designation,
    String payPeriod,
    String currency,
    OrganizationProfile organizationProfile,
    EmployeeProfile employeeProfile,
    BigDecimal basicSalary,
    List<PayrollLineItem> allowances,
    List<PayrollLineItem> deductions,
    BigDecimal taxPercent,
    BigDecimal providentFundPercent,
    BigDecimal taxAmount,
    BigDecimal providentFundAmount,
    BigDecimal grossEarnings,
    BigDecimal totalDeductions,
    BigDecimal netPay,
    Instant generatedAt,
    String generatedByEmail,
    String generatedByUserId,
    AuthDependencyStatus authDependencyStatus
) {
}
