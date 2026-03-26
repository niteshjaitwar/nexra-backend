package com.nexra.hrms.nexra.modules.payroll.model;

import java.math.BigDecimal;
import java.time.Instant;

public record EmployeeProfile(
    String tenantCode,
    String employeeId,
    String employeeCode,
    String employeeName,
    String department,
    String designation,
    BigDecimal monthlyBasicSalary,
    String bankName,
    String bankAccountMasked,
    String panMasked,
    String uanMasked,
    String email,
    Instant updatedAt,
    String updatedBy
) {
}
