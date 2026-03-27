package com.nexra.hrms.nexra.modules.hrms.employee.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import com.nexra.hrms.nexra.modules.hrms.employee.validation.TenantCode;

public record OrganizationProfileUpsertRequest(
    @NotBlank @TenantCode @Size(max = 60) String tenantCode,
    @NotBlank @Size(max = 160) String organizationName,
    @NotBlank @Size(max = 160) String legalEntityName,
    @NotBlank @Size(max = 200) String addressLine1,
    @Size(max = 200) String addressLine2,
    @NotBlank @Size(max = 100) String city,
    @NotBlank @Size(max = 100) String state,
    @NotBlank @Size(max = 100) String country,
    @NotBlank @Size(max = 30) String postalCode,
    @NotBlank @Size(max = 12) String currency,
    @DecimalMin("0.00") BigDecimal defaultTaxPercent,
    @DecimalMin("0.00") BigDecimal defaultProvidentFundPercent,
    @Email @Size(max = 160) String payrollContactEmail,
    @Size(max = 40) String payrollContactPhone
) {
}
