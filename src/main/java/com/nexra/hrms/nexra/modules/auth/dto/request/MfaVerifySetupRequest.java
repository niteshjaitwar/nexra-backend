package com.nexra.hrms.nexra.modules.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MfaVerifySetupRequest(
    @NotBlank @Size(min = 6, max = 6) String code
) {
}
