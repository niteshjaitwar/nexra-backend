package com.nexra.hrms.nexra.modules.crm.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request to transition a quote to a new lifecycle status.
 */
public record CrmQuoteStatusUpdateRequest(
    @NotBlank @Size(max = 40) String targetStatus
) {
}
