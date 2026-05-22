package com.nexra.hrms.nexra.modules.crm.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CrmDealCreateRequest(
    @Size(max = 36) String accountId,
    @Size(max = 36) String contactId,
    @NotBlank @Size(max = 200) String title,
    @NotBlank @Size(max = 40) String stage,
    @DecimalMin(value = "0.00", inclusive = false) BigDecimal valueAmount,
    @Size(max = 10) String currency,
    @NotBlank @Size(max = 36) String ownerUserId,
    LocalDate expectedCloseDate
) {
}

