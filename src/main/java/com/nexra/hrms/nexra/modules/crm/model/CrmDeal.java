package com.nexra.hrms.nexra.modules.crm.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record CrmDeal(
    String id,
    String tenantCode,
    String accountId,
    String contactId,
    String title,
    String stage,
    BigDecimal valueAmount,
    String currency,
    String ownerUserId,
    LocalDate expectedCloseDate,
    Instant createdAt,
    Instant updatedAt
) {
}

