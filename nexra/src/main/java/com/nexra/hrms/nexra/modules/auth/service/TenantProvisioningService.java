package com.nexra.hrms.nexra.modules.auth.service;

import com.nexra.hrms.nexra.modules.auth.dto.request.TenantProvisionRequest;
import com.nexra.hrms.nexra.modules.auth.dto.response.TenantProvisionResponse;

/**
 * Orchestrates end-to-end onboarding of a new SME tenant including user and product access creation.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
public interface TenantProvisioningService {

    /**
     * Creates a new tenant, admin user, and product access grants in a single atomic operation.
     *
     * @param request tenant provisioning payload
     * @return provisioning summary response
     */
    TenantProvisionResponse provision(TenantProvisionRequest request);
}
