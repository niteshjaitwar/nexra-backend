package com.nexra.hrms.nexra.modules.operations.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record OpsProjectCreateRequest(
    @NotBlank @Size(max = 60) String code,
    @NotBlank @Size(max = 200) String name,
    @Size(max = 4000) String description,
    @NotBlank @Size(max = 36) String ownerUserId,
    @Size(max = 36) String crmDealId,
    @Size(max = 60) String departmentCode,
    LocalDate startDate,
    LocalDate endDate
) {
}
