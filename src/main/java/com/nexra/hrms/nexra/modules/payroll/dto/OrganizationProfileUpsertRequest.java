package com.nexra.hrms.nexra.modules.payroll.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public record OrganizationProfileUpsertRequest(
    @NotBlank String tenantCode,
    @NotBlank String organizationName,
    @NotBlank String legalEntityName,
    @NotBlank String addressLine1,
    String addressLine2,
    @NotBlank String city,
    @NotBlank String state,
    @NotBlank String country,
    @NotBlank String postalCode,
    @NotBlank String currency,
    @DecimalMin(value = "0.00", inclusive = true) BigDecimal defaultTaxPercent,
    @DecimalMin(value = "0.00", inclusive = true) BigDecimal defaultProvidentFundPercent,
    @Email String payrollContactEmail,
    String payrollContactPhone
) {
}
