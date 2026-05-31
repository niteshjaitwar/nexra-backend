package com.nexra.hrms.nexra.modules.auth.dto.request;

import com.nexra.hrms.nexra.modules.auth.validation.TenantCode;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordResetConfirmRequest(
    @NotBlank @TenantCode String tenantCode,
    @NotBlank @Email String email,
    @NotBlank @Size(min = 6, max = 6) String otp,
    @NotBlank @Size(min = 8, max = 128) String newPassword
) {
}
