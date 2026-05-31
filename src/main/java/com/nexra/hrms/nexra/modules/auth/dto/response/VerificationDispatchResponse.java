package com.nexra.hrms.nexra.modules.auth.dto.response;

/**
 * Represents data contract for VerificationDispatchResponse.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
public record VerificationDispatchResponse(
    String channel,
    String destination,
    String hint
) {
}
