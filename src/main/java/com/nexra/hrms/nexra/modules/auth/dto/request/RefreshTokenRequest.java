package com.nexra.hrms.nexra.modules.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Represents data contract for RefreshTokenRequest.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
public record RefreshTokenRequest(
    @NotBlank String refreshToken
) {
}
