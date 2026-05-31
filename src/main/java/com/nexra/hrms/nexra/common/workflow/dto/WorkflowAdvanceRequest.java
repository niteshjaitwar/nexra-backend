package com.nexra.hrms.nexra.common.workflow.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request to advance or reject a workflow instance at its current step.
 */
public record WorkflowAdvanceRequest(
    @NotNull Boolean approve,
    @Size(max = 2000) String notes
) {
}
