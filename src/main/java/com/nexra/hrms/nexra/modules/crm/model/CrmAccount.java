package com.nexra.hrms.nexra.modules.crm.model;

import java.time.Instant;

public record CrmAccount(
    String id,
    String tenantCode,
    String name,
    String website,
    String industry,
    String ownerUserId,
    Instant createdAt,
    Instant updatedAt
) {
}

