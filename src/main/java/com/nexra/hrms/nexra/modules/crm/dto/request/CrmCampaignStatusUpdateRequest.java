package com.nexra.hrms.nexra.modules.crm.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request to transition a campaign to a new lifecycle status.
 */
public record CrmCampaignStatusUpdateRequest(
    @NotBlank @Size(max = 40) String targetStatus
) {
}
