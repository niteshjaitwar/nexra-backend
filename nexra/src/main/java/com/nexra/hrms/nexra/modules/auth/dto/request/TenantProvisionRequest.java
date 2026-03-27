package com.nexra.hrms.nexra.modules.auth.dto.request;

import com.nexra.hrms.nexra.modules.auth.enums.ProductType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Set;

/**
 * Represents data contract for TenantProvisionRequest.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
public record TenantProvisionRequest(
    @NotBlank @Size(max = 60) String tenantCode,
    @NotBlank @Size(max = 120) String companyName,
    @NotBlank @Email String adminEmail,
    @NotBlank @Size(max = 80) String adminFirstName,
    @NotBlank @Size(max = 80) String adminLastName,
    @NotEmpty Set<@NotNull ProductType> products
) {
}
