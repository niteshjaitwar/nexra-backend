package com.nexra.hrms.nexra.modules.crm.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record CrmActivityCreateRequest(
    @Size(max = 36)
    String leadId,
    @Size(max = 36)
    String contactId,
    @Size(max = 36)
    String dealId,
    @NotBlank
    @Size(max = 40)
    String activityType,
    @Size(max = 2000)
    String notes,
    Instant occurredAt,
    @NotBlank
    @Size(max = 36)
    String ownerUserId
) {

    @AssertTrue(message = "Exactly one of leadId, contactId, or dealId must be provided.")
    public boolean isLinkedToExactlyOneRecord() {
        int count = 0;
        count += hasText(leadId) ? 1 : 0;
        count += hasText(contactId) ? 1 : 0;
        count += hasText(dealId) ? 1 : 0;
        return count == 1;
    }

    private boolean hasText(final String value) {
        return value != null && !value.isBlank();
    }
}
