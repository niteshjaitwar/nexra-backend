package com.nexra.hrms.nexra.modules.auth.dto.response;

import java.util.List;

public record MfaEnableResponse(
    List<String> recoveryCodes
) {
}
