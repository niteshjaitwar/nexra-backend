package com.nexra.hrms.nexra.modules.auth.controller;

import com.nexra.hrms.nexra.modules.auth.dto.request.OAuthClientCreateRequest;
import com.nexra.hrms.nexra.modules.auth.dto.response.ApiResponse;
import com.nexra.hrms.nexra.modules.auth.dto.response.OAuthClientResponse;
import com.nexra.hrms.nexra.modules.auth.service.oauth.OAuthClientService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes secured OAuth2 client management API endpoints.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/oauth-clients")
public class OAuthClientController {

    private final OAuthClientService oAuthClientService;

    /**
     * Registers a new OAuth client for trusted integrations.
     *
     * @param request OAuth client registration payload
     * @return standardized API response with created client metadata
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<OAuthClientResponse>> registerClient(
        @Valid @RequestBody final OAuthClientCreateRequest request
    ) {
        log.info("OAuthClientController() - registerClient() - Register client endpoint invoked, clientId={}", request.clientId());
        OAuthClientResponse response = oAuthClientService.registerClient(request);
        return ResponseEntity.ok(ApiResponse.success("OAuth client registered successfully.", response));
    }

    /**
     * Lists all registered OAuth clients.
     *
     * @return standardized API response with registered client metadata
     */
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<List<OAuthClientResponse>>> listClients() {
        log.info("OAuthClientController() - listClients() - List clients endpoint invoked");
        List<OAuthClientResponse> response = oAuthClientService.listClients();
        return ResponseEntity.ok(ApiResponse.success("OAuth clients fetched successfully.", response));
    }
}
