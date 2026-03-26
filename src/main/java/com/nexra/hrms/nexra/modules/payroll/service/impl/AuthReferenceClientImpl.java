package com.nexra.hrms.nexra.modules.payroll.service.impl;

import com.nexra.hrms.nexra.modules.auth.repository.TenantRepository;
import com.nexra.hrms.nexra.modules.payroll.model.AuthDependencyStatus;
import com.nexra.hrms.nexra.modules.payroll.service.AuthReferenceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Resolves auth dependency status through direct in-process module access.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthReferenceClientImpl implements AuthReferenceClient {

    private final TenantRepository tenantRepository;

    @Override
    public AuthDependencyStatus getAuthHealth() {
        try {
            long tenantCount = tenantRepository.count();
            return new AuthDependencyStatus(true, "UP", "Auth module reachable in-process. tenantCount=" + tenantCount);
        } catch (RuntimeException ex) {
            log.warn("Auth service health check failed: {}", ex.getMessage());
            return new AuthDependencyStatus(false, "DOWN", ex.getMessage());
        }
    }
}
