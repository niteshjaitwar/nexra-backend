package com.nexra.hrms.nexra.common.workflow.model;

import java.time.Instant;

/**
 * A single step transition in a workflow instance history.
 */
public record WorkflowStepHistory(
    String id,
    String instanceId,
    int stepIndex,
    String stepName,
    String action,
    String actorEmail,
    String notes,
    Instant createdAt
) {
}
