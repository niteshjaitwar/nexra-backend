package com.nexra.hrms.nexra.modules.auth.dto.request;

import com.nexra.hrms.nexra.modules.auth.enums.VerificationPurpose;
import com.nexra.hrms.nexra.modules.auth.validation.TenantCode;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Represents data contract for LinkVerificationRequest.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
public record LinkVerificationRequest(
    @NotBlank @TenantCode String tenantCode,
    @NotBlank @Email String email,
    @NotNull VerificationPurpose purpose,
    @NotBlank String token
) {
}
