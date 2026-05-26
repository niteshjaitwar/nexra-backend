package com.nexra.hrms.nexra.modules.crm.model;

import java.time.Instant;

public record CrmWorkflowRule(
    String id,
    String tenantCode,
    String moduleKey,
    String name,
    String triggerEvent,
    String criteriaJson,
    String actionsJson,
    int priority,
    boolean active,
    Instant createdAt,
    Instant updatedAt
) {
}
