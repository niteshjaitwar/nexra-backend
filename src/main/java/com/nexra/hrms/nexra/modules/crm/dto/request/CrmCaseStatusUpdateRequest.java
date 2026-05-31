package com.nexra.hrms.nexra.modules.crm.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CrmCaseStatusUpdateRequest(
    @NotBlank @Size(max = 40) String targetStatus,
    @Size(max = 2000) String note
) {
}
