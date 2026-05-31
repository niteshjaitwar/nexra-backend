package com.nexra.hrms.nexra.common.workflow.model;

import java.time.Instant;

/**
 * API representation of a workflow instance with step and SLA state.
 */
public record WorkflowInstance(
    String id,
    String tenantCode,
    String productCode,
    String moduleKey,
    String triggerEvent,
    String status,
    String actorEmail,
    int currentStepIndex,
    String currentStepName,
    String stepStatus,
    Instant slaDueAt,
    boolean escalated,
    Instant escalatedAt,
    Instant completedAt,
    Instant createdAt
) {
}
