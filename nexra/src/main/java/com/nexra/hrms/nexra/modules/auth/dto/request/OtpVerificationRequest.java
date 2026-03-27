package com.nexra.hrms.nexra.modules.auth.dto.request;

import com.nexra.hrms.nexra.modules.auth.enums.VerificationPurpose;
import com.nexra.hrms.nexra.modules.auth.validation.TenantCode;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Represents data contract for OtpVerificationRequest.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
public record OtpVerificationRequest(
    @NotBlank @TenantCode String tenantCode,
    @NotBlank @Email String email,
    @NotNull VerificationPurpose purpose,
    @NotBlank @Size(min = 6, max = 6) String otp
) {
}
