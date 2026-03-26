package com.nexra.hrms.nexra.modules.auth.dto.request;

import com.nexra.hrms.nexra.modules.auth.enums.AccountType;
import com.nexra.hrms.nexra.modules.auth.validation.StrongPassword;
import com.nexra.hrms.nexra.modules.auth.validation.TenantCode;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Represents data contract for RegisterRequest.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
public record RegisterRequest(
    @NotBlank @TenantCode String tenantCode,
    @NotBlank @Email String email,
    @NotBlank @Size(min = 12, max = 160) @StrongPassword String password,
    @NotBlank @Size(max = 80) String firstName,
    @NotBlank @Size(max = 80) String lastName,
    @NotNull AccountType accountType
) {
}
