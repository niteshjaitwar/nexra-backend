package com.nexra.hrms.nexra.modules.operations.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request to update an operations task status.
 */
public record OpsTaskStatusUpdateRequest(
    @NotBlank @Size(max = 40) String status
) {
}
