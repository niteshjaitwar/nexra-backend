package com.nexra.hrms.nexra.modules.auth.controller;

import com.nexra.hrms.nexra.modules.auth.dto.request.OAuthClientCreateRequest;
import com.nexra.hrms.nexra.common.api.ApiResponse;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Exposes secured OAuth2 client management API endpoints.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@Slf4j
@Tag(name = "Authentication", description = "Authentication and identity APIs.")
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
    @Operation(summary = "POST /api/v1/oauth-clients", description = "Processes POST requests for /api/v1/oauth-clients.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
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
    @Operation(summary = "GET /api/v1/oauth-clients", description = "Processes GET requests for /api/v1/oauth-clients.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<List<OAuthClientResponse>>> listClients() {
        log.info("OAuthClientController() - listClients() - List clients endpoint invoked");
        List<OAuthClientResponse> response = oAuthClientService.listClients();
        return ResponseEntity.ok(ApiResponse.success("OAuth clients fetched successfully.", response));
    }
}
