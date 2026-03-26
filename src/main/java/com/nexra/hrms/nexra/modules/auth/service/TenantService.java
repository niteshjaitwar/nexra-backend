package com.nexra.hrms.nexra.modules.auth.service;

import com.nexra.hrms.nexra.modules.auth.dto.request.TenantCreateRequest;
import com.nexra.hrms.nexra.modules.auth.dto.response.TenantResponse;
import com.nexra.hrms.nexra.modules.auth.entity.Tenant;

/**
 * Provides tenant onboarding and retrieval capabilities for multi-tenant auth flows.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
public interface TenantService {

    /**
     * Creates and activates a new tenant.
     *
     * @param request tenant creation payload
     * @return created tenant response
     */
    TenantResponse createTenant(TenantCreateRequest request);

    /**
     * Resolves an active tenant by code.
     *
     * @param tenantCode tenant unique code
     * @return active tenant entity
     */
    Tenant resolveActiveTenant(String tenantCode);
}
