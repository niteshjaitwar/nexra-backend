package com.nexra.hrms.nexra.modules.crm.model;

import java.time.Instant;
import java.util.Map;

public record CrmContact(
    String id,
    String tenantCode,
    String accountId,
    String fullName,
    String email,
    String phone,
    String ownerUserId,
    Instant createdAt,
    Instant updatedAt,
    Map<String, Object> customFields
) {
}
