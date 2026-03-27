package com.nexra.hrms.nexra.modules.payroll.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record EmployeeProfileUpsertRequest(
    @NotBlank String tenantCode,
    @NotBlank String employeeId,
    @NotBlank String employeeCode,
    @NotBlank String employeeName,
    @NotBlank String department,
    @NotBlank String designation,
    @NotNull @DecimalMin(value = "0.00", inclusive = false) BigDecimal monthlyBasicSalary,
    String bankName,
    String bankAccountMasked,
    String panMasked,
    String uanMasked,
    @Email String email
) {
}
