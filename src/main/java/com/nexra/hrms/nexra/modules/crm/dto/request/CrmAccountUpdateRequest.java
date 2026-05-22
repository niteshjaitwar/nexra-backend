package com.nexra.hrms.nexra.modules.crm.dto.request;

import jakarta.validation.constraints.Size;

public record CrmAccountUpdateRequest(
    @Size(max = 180) String name,
    @Size(max = 200) String website,
    @Size(max = 80) String industry,
    @Size(max = 36) String ownerUserId
) {
}

