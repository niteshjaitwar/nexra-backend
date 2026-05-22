package com.nexra.hrms.nexra.modules.crm.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CrmDealUpdateRequest(
    @Size(max = 36) String accountId,
    @Size(max = 36) String contactId,
    @Size(max = 200) String title,
    @Size(max = 40) String stage,
    @DecimalMin(value = "0.00", inclusive = false) BigDecimal valueAmount,
    @Size(max = 10) String currency,
    @Size(max = 36) String ownerUserId,
    LocalDate expectedCloseDate
) {
}

