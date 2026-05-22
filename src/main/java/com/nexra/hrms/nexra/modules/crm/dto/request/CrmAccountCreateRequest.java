package com.nexra.hrms.nexra.modules.crm.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CrmAccountCreateRequest(
    @NotBlank @Size(max = 180) String name,
    @Size(max = 200) String website,
    @Size(max = 80) String industry,
    @NotBlank @Size(max = 36) String ownerUserId
) {
}

