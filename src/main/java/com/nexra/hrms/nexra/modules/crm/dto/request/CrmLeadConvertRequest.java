package com.nexra.hrms.nexra.modules.crm.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CrmLeadConvertRequest(
    @Size(max = 200) String dealTitle,
    @Size(max = 40) String stage,
    @DecimalMin(value = "0.00", inclusive = false) BigDecimal valueAmount,
    @Size(max = 10) String currency,
    LocalDate expectedCloseDate
) {
}

