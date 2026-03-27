package com.nexra.hrms.nexra.modules.auth.dto.request;

import com.nexra.hrms.nexra.modules.auth.validation.TenantCode;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Represents data contract for LoginRequest.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
public record LoginRequest(
    @NotBlank @TenantCode String tenantCode,
    @NotBlank @Email String email,
    @NotBlank String password
) {
}
