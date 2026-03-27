package com.nexra.hrms.nexra.modules.hrms.employee.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record Employee(
    String employeeId,
    String tenantCode,
    String employeeCode,
    String firstName,
    String lastName,
    String fullName,
    String workEmail,
    String departmentId,
    String designation,
    String status,
    LocalDate joinDate,
    BigDecimal monthlyBasicSalary,
    String bankName,
    String bankAccountMasked,
    String panMasked,
    String uanMasked,
    boolean active,
    Instant updatedAt,
    String updatedBy
) {
}
