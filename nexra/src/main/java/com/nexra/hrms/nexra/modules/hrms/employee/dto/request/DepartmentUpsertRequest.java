package com.nexra.hrms.nexra.modules.hrms.employee.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.nexra.hrms.nexra.modules.hrms.employee.validation.TenantCode;

public record DepartmentUpsertRequest(
    String departmentId,
    @NotBlank @TenantCode @Size(max = 60) String tenantCode,
    @NotBlank @Size(max = 60) String code,
    @NotBlank @Size(max = 120) String name,
    @Size(max = 36) String managerEmployeeId,
    Boolean active
) {
}
