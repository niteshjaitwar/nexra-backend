package com.nexra.hrms.nexra.modules.auth.dto.request;

import com.nexra.hrms.nexra.modules.auth.validation.TenantCode;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Represents data contract for LoginRequest.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
public record LoginRequest(
    @NotBlank @TenantCode String tenantCode,
    @NotBlank @Email String email,
    @NotBlank String password,
    @Size(min = 6, max = 6) String mfaCode,
    @Size(min = 8, max = 32) String recoveryCode
) {
}
