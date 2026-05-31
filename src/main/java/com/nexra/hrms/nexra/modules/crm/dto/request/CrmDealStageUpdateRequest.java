package com.nexra.hrms.nexra.modules.crm.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request to transition a deal to a new pipeline stage.
 */
public record CrmDealStageUpdateRequest(
    @NotBlank @Size(max = 40) String targetStage
) {
}
