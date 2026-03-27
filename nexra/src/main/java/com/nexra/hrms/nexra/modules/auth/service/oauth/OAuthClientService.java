package com.nexra.hrms.nexra.modules.auth.service.oauth;

import com.nexra.hrms.nexra.modules.auth.dto.request.OAuthClientCreateRequest;
import com.nexra.hrms.nexra.modules.auth.dto.response.OAuthClientResponse;
import java.util.List;

/**
 * Handles OAuth2 client registration and discovery for trusted enterprise integrations.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
public interface OAuthClientService {

    /**
     * Registers a new OAuth2 client in authorization server storage.
     *
     * @param request client registration request
     * @return created client metadata
     */
    OAuthClientResponse registerClient(OAuthClientCreateRequest request);

    /**
     * Lists registered OAuth2 clients metadata.
     *
     * @return registered clients
     */
    List<OAuthClientResponse> listClients();
}
