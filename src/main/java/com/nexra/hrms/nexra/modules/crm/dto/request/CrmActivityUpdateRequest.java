package com.nexra.hrms.nexra.modules.crm.dto.request;

import jakarta.validation.constraints.Size;
import java.time.Instant;

public record CrmActivityUpdateRequest(
    @Size(max = 40) String activityType,
    @Size(max = 4000) String notes,
    Instant occurredAt,
    @Size(max = 36) String ownerUserId
) {
}
