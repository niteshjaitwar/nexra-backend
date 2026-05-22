package com.nexra.hrms.nexra.modules.payroll.model;

import java.math.BigDecimal;
import java.time.Instant;

public record OrganizationProfile(
    String tenantCode,
    String organizationName,
    String legalEntityName,
    String addressLine1,
    String addressLine2,
    String city,
    String state,
    String country,
    String postalCode,
    String currency,
    BigDecimal defaultTaxPercent,
    BigDecimal defaultProvidentFundPercent,
    String payrollContactEmail,
    String payrollContactPhone,
    String brandingLogoPath,
    String brandingCompanyName,
    String brandingWatermarkText,
    Instant updatedAt,
    String updatedBy
) {
}
