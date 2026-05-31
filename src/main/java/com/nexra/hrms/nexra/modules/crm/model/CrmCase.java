package com.nexra.hrms.nexra.modules.crm.model;

import java.time.Instant;

public record CrmCase(
    String id,
    String tenantCode,
    String subject,
    String description,
    String status,
    String priority,
    String accountId,
    String contactId,
    String ownerUserId,
    Instant slaDueAt
) {
}
