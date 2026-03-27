package com.nexra.hrms.nexra.modules.auth.dto.response;

/**
 * Represents data contract for TokenPairResponse.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
public record TokenPairResponse(
    String accessToken,
    String refreshToken,
    String tokenType,
    long expiresInSeconds,
    UserProfileResponse user
) {
}
