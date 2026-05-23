package com.nexra.hrms.nexra.modules.auth.controller;

import com.nexra.hrms.nexra.modules.auth.dto.request.TenantProvisionRequest;
import com.nexra.hrms.nexra.common.api.ApiResponse;
import com.nexra.hrms.nexra.modules.auth.dto.response.TenantProvisionResponse;
import com.nexra.hrms.nexra.modules.auth.service.TenantProvisioningService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Exposes platform admin APIs for SME tenant provisioning and management.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@Slf4j
@Tag(name = "Authentication", description = "Authentication and identity APIs.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/platform")
public class PlatformAdminController {

    private final TenantProvisioningService tenantProvisioningService;

    /**
     * Provisions a new SME tenant with admin user and product access in a single operation.
     *
     * @param request tenant provisioning payload
     * @return standardized API response with provisioning summary
     */
    @Operation(summary = "POST endpoint", description = "Handles POST requests for this resource.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
    @PostMapping("/tenants/provision")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<TenantProvisionResponse>> provisionTenant(
        @Valid @RequestBody final TenantProvisionRequest request
    ) {
        log.info("PlatformAdminController() - provisionTenant() - Provision endpoint invoked, tenantCode={}, products={}",
            request.tenantCode(), request.products());
        TenantProvisionResponse response = tenantProvisioningService.provision(request);
        return ResponseEntity.ok(ApiResponse.success("Tenant provisioned successfully.", response));
    }
}
