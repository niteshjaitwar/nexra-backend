package com.nexra.hrms.nexra.modules.hrms.employee.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import com.nexra.hrms.nexra.modules.hrms.employee.validation.TenantCode;

public record EmployeeUpsertRequest(
    String employeeId,
    @NotBlank @TenantCode @Size(max = 60) String tenantCode,
    @NotBlank @Size(max = 60) String employeeCode,
    @NotBlank @Size(max = 80) String firstName,
    @NotBlank @Size(max = 80) String lastName,
    @NotBlank @Email @Size(max = 160) String workEmail,
    @Size(max = 36) String departmentId,
    @NotBlank @Size(max = 120) String designation,
    @NotBlank @Size(max = 40) String status,
    @NotNull LocalDate joinDate,
    @NotNull @DecimalMin("0.00") BigDecimal monthlyBasicSalary,
    @Size(max = 120) String bankName,
    @Size(max = 50) String bankAccountMasked,
    @Size(max = 30) String panMasked,
    @Size(max = 30) String uanMasked,
    Boolean active
) {
}
