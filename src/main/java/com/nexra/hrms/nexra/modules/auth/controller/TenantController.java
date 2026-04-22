package com.nexra.hrms.nexra.modules.auth.controller;

import com.nexra.hrms.nexra.modules.auth.dto.request.TenantCreateRequest;
import com.nexra.hrms.nexra.common.api.ApiResponse;
import com.nexra.hrms.nexra.modules.auth.dto.response.TenantResponse;
import com.nexra.hrms.nexra.modules.auth.service.TenantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes tenant onboarding API endpoints.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tenants")
public class TenantController {

    private final TenantService tenantService;

    /**
     * Creates a new tenant for enterprise onboarding.
     *
     * @param request tenant creation payload
     * @return standardized API response with tenant data
     */
    @PostMapping
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<TenantResponse>> createTenant(@Valid @RequestBody final TenantCreateRequest request) {
        log.info("TenantController() - createTenant() - Tenant creation endpoint invoked, code={}, enterprise={}", request.code(), request.enterprise());
        TenantResponse response = tenantService.createTenant(request);
        return ResponseEntity.ok(ApiResponse.success("Tenant created successfully.", response));
    }
}
