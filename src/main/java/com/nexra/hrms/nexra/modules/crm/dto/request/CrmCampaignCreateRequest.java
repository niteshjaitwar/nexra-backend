package com.nexra.hrms.nexra.modules.crm.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request to create a marketing campaign.
 */
public record CrmCampaignCreateRequest(
    @NotBlank @Size(max = 240) String name,
    @NotBlank @Size(max = 40) String campaignType,
    @Size(max = 4000) String description,
    @PositiveOrZero BigDecimal budget,
    @PositiveOrZero BigDecimal actualCost,
    LocalDate startDate,
    LocalDate endDate,
    @NotBlank @Size(max = 36) String ownerUserId
) {
}
