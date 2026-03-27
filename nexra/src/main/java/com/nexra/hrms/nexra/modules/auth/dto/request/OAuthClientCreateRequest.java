package com.nexra.hrms.nexra.modules.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.Set;

/**
 * Represents data contract for OAuthClientCreateRequest.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
public record OAuthClientCreateRequest(
    @NotBlank @Size(max = 100) String clientId,
    @NotBlank @Size(min = 16, max = 200) String clientSecret,
    @NotBlank @Size(max = 200) String clientName,
    @NotBlank @Size(max = 500) String redirectUri,
    @NotEmpty Set<String> scopes
) {
}
