package com.nexra.hrms.nexra.modules.hrms.attendance.dto.request;

import com.nexra.hrms.nexra.modules.hrms.employee.validation.TenantCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AttendanceRegularizationDecisionRequest(
    @NotBlank @TenantCode String tenantCode,
    @Size(max = 500) String decisionComment
) {
}
