package com.nexra.hrms.nexra.modules.auth.dto.response;

/**
 * Represents data contract for VerificationResultResponse.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
public record VerificationResultResponse(
    String status,
    TokenPairResponse tokens
) {
}
