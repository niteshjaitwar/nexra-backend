package com.nexra.hrms.nexra.modules.auth.service.impl;

import com.nexra.hrms.nexra.modules.auth.dto.request.TenantCreateRequest;
import com.nexra.hrms.nexra.modules.auth.dto.response.TenantResponse;
import com.nexra.hrms.nexra.modules.auth.entity.Tenant;
import com.nexra.hrms.nexra.modules.auth.exception.BusinessException;
import com.nexra.hrms.nexra.modules.auth.exception.ResourceNotFoundException;
import com.nexra.hrms.nexra.modules.auth.repository.TenantRepository;
import com.nexra.hrms.nexra.modules.auth.service.TenantService;
import com.nexra.hrms.nexra.modules.auth.service.security.SecurityAuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles tenant lifecycle operations for multi-tenant authentication boundaries.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TenantServiceImpl implements TenantService {

    private final TenantRepository tenantRepository;
    private final SecurityAuditService securityAuditService;
    private final ModelMapper modelMapper;

    /**
     * Creates and persists a new active tenant.
     *
     * @param request tenant onboarding payload
     * @return created tenant details
     */
    @Override
    @Transactional
    public TenantResponse createTenant(final TenantCreateRequest request) {
        log.info("TenantServiceImpl() - createTenant() - Creating tenant, code={}", request.code());
        if (tenantRepository.existsByCodeIgnoreCase(request.code())) {
            securityAuditService.record("TENANT_CREATE", request.code(), null, "FAILURE", "Duplicate tenant code rejected.");
            throw new BusinessException("Tenant code already exists.");
        }

        Tenant tenant = modelMapper.map(request, Tenant.class);
        tenant.setCode(request.code().trim().toLowerCase());
        tenant.setActive(true);

        Tenant saved = tenantRepository.save(tenant);
        securityAuditService.record("TENANT_CREATE", saved.getCode(), null, "SUCCESS", "Tenant created.");
        log.info("TenantServiceImpl() - createTenant() - Tenant created successfully, code={}", saved.getCode());
        return modelMapper.map(saved, TenantResponse.class);
    }

    /**
     * Resolves active tenant for tenant-scoped requests.
     *
     * @param tenantCode tenant unique key
     * @return active tenant
     */
    @Override
    @Transactional(readOnly = true)
    public Tenant resolveActiveTenant(final String tenantCode) {
        log.info("TenantServiceImpl() - resolveActiveTenant() - Resolving tenant, tenantCode={}", tenantCode);
        return tenantRepository.findByCodeIgnoreCaseAndActiveTrue(tenantCode)
            .orElseThrow(() -> new ResourceNotFoundException("Active tenant not found for code: " + tenantCode));
    }
}
