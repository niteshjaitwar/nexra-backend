package com.nexra.hrms.nexra.modules.crm.model;

import java.time.Instant;

public record CrmActivity(
    String id,
    String tenantCode,
    String leadId,
    String contactId,
    String dealId,
    String activityType,
    String notes,
    Instant occurredAt,
    String ownerUserId,
    Instant createdAt,
    Instant updatedAt
) {
}
