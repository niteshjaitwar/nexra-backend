package com.nexra.hrms.nexra.modules.auth.dto.response;

import java.time.Instant;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

/**
 * Defines OAuthClientResponse component.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@Getter
@Setter
public class OAuthClientResponse {

    private String id;
    private String clientId;
    private String clientName;
    private String redirectUri;
    private Set<String> scopes;
    private Instant clientIdIssuedAt;
}
