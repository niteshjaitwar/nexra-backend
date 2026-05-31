package com.nexra.hrms.nexra.modules.crm.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CrmCaseAssignRequest(
    @NotBlank @Size(max = 36) String ownerUserId
) {
}
