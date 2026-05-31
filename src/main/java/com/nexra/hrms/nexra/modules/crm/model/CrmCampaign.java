package com.nexra.hrms.nexra.modules.crm.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * API representation of a marketing campaign.
 */
public record CrmCampaign(
    String id,
    String tenantCode,
    String name,
    String campaignType,
    String status,
    String description,
    BigDecimal budget,
    BigDecimal actualCost,
    LocalDate startDate,
    LocalDate endDate,
    String ownerUserId
) {
}
