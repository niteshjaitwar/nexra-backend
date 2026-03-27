package com.nexra.hrms.nexra.modules.auth.dto.response.dev;

/**
 * Represents data contract for DevDbStatsResponse.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
public record DevDbStatsResponse(
    long tenantCount,
    long userCount,
    long verificationTokenCount,
    long refreshTokenCount
) {
}
