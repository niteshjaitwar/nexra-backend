package com.nexra.hrms.nexra.modules.auth.service.impl;

import com.nexra.hrms.nexra.modules.auth.dto.request.TenantProvisionRequest;
import com.nexra.hrms.nexra.modules.auth.dto.response.TenantProvisionResponse;
import com.nexra.hrms.nexra.modules.auth.entity.Tenant;
import com.nexra.hrms.nexra.modules.auth.entity.UserAccount;
import com.nexra.hrms.nexra.modules.auth.entity.UserProductAccess;
import com.nexra.hrms.nexra.modules.auth.enums.AccountType;
import com.nexra.hrms.nexra.modules.auth.enums.ProductRole;
import com.nexra.hrms.nexra.modules.auth.enums.ProductType;
import com.nexra.hrms.nexra.modules.auth.enums.UserRole;
import com.nexra.hrms.nexra.modules.auth.enums.UserStatus;
import com.nexra.hrms.nexra.modules.auth.exception.BusinessException;
import com.nexra.hrms.nexra.modules.auth.repository.TenantRepository;
import com.nexra.hrms.nexra.modules.auth.repository.UserAccountRepository;
import com.nexra.hrms.nexra.modules.auth.repository.UserProductAccessRepository;
import com.nexra.hrms.nexra.modules.auth.service.TenantProvisioningService;
import com.nexra.hrms.nexra.modules.auth.service.notification.NotificationService;
import com.nexra.hrms.nexra.modules.auth.service.security.SecurityAuditService;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orchestrates end-to-end onboarding of a new SME tenant including user and product access creation.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TenantProvisioningServiceImpl implements TenantProvisioningService {

    private final TenantRepository tenantRepository;
    private final UserAccountRepository userAccountRepository;
    private final UserProductAccessRepository userProductAccessRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;
    private final SecurityAuditService securityAuditService;

    /**
     * Creates a new tenant, admin user, and product access grants in a single atomic operation.
     * A temporary password is generated and the admin receives a welcome OTP via email.
     *
     * @param request tenant provisioning payload
     * @return provisioning summary response
     */
    @Override
    @Transactional
    public TenantProvisionResponse provision(final TenantProvisionRequest request) {
        log.info("TenantProvisioningServiceImpl() - provision() - Provisioning new tenant, tenantCode={}, products={}",
            request.tenantCode(), request.products());

        if (tenantRepository.existsByCodeIgnoreCase(request.tenantCode())) {
            securityAuditService.record("TENANT_PROVISION", request.tenantCode(), request.adminEmail(), "FAILURE",
                "Duplicate tenant code rejected.");
            throw new BusinessException("Tenant code already exists: " + request.tenantCode());
        }

        Tenant tenant = new Tenant();
        tenant.setCode(request.tenantCode().trim().toLowerCase());
        tenant.setName(request.companyName());
        tenant.setEnterprise(true);
        tenant.setActive(true);
        Tenant savedTenant = tenantRepository.save(tenant);
        log.info("TenantProvisioningServiceImpl() - provision() - Tenant created, tenantCode={}", savedTenant.getCode());

        String tempPassword = UUID.randomUUID().toString().replace("-", "").substring(0, 12) + "A1!";
        UserAccount admin = new UserAccount();
        admin.setTenant(savedTenant);
        admin.setEmail(request.adminEmail().trim().toLowerCase());
        admin.setFirstName(request.adminFirstName());
        admin.setLastName(request.adminLastName());
        admin.setPasswordHash(passwordEncoder.encode(tempPassword));
        admin.setStatus(UserStatus.ACTIVE);
        admin.setEmailVerified(true);
        admin.setMfaEnabled(false);
        admin.setAccountType(AccountType.ENTERPRISE);
        admin.setRoles(Set.of(UserRole.ROLE_TENANT_ADMIN));
        UserAccount savedAdmin = userAccountRepository.save(admin);
        log.info("TenantProvisioningServiceImpl() - provision() - Admin user created, email={}", maskEmail(request.adminEmail()));

        for (ProductType product : request.products()) {
            UserProductAccess access = new UserProductAccess();
            access.setUser(savedAdmin);
            access.setProduct(product);
            access.setProductRole(ProductRole.TENANT_ADMIN);
            access.setGrantedAt(Instant.now());
            userProductAccessRepository.save(access);
        }
        securityAuditService.record("TENANT_PROVISION", savedTenant.getCode(), savedAdmin.getEmail(), "SUCCESS",
            "Tenant provisioned with products=" + productNames(request));
        log.info("TenantProvisioningServiceImpl() - provision() - Product access granted, tenantCode={}, products={}",
            savedTenant.getCode(), request.products());

        notificationService.sendOtp(request.adminEmail(), tempPassword);

        Set<String> productNames = request.products().stream()
            .map(ProductType::name)
            .collect(Collectors.toSet());

        TenantProvisionResponse response = new TenantProvisionResponse();
        response.setTenantCode(savedTenant.getCode());
        response.setCompanyName(savedTenant.getName());
        response.setAdminEmail(savedAdmin.getEmail());
        response.setGrantedProducts(productNames);
        response.setMessage("Tenant provisioned successfully. Admin credentials sent via email.");

        log.info("TenantProvisioningServiceImpl() - provision() - Provisioning complete, tenantCode={}", savedTenant.getCode());
        return response;
    }

    private Set<String> productNames(final TenantProvisionRequest request) {
        return request.products().stream()
            .map(ProductType::name)
            .collect(Collectors.toSet());
    }

    private String maskEmail(final String email) {
        if (email == null || email.isBlank()) {
            return "-";
        }
        int at = email.indexOf('@');
        if (at <= 1) {
            return "***";
        }
        return email.charAt(0) + "***" + email.substring(at);
    }
}
