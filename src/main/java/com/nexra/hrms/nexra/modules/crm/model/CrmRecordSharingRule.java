package com.nexra.hrms.nexra.modules.crm.model;

import java.time.Instant;

public record CrmRecordSharingRule(
    String id,
    String tenantCode,
    String moduleKey,
    String name,
    String criteriaJson,
    String principalType,
    String principalKey,
    String accessLevel,
    boolean active,
    Instant createdAt,
    Instant updatedAt
) {
}
