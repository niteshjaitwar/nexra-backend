package com.nexra.hrms.nexra.modules.auth.dto.response;

public record MfaSetupResponse(
    String secret,
    String otpAuthUri
) {
}
