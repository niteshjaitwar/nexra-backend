package com.nexra.hrms.nexra.modules.auth.service.impl;

import com.nexra.hrms.nexra.modules.auth.dto.request.GrantProductAccessRequest;
import com.nexra.hrms.nexra.modules.auth.dto.response.ProductAccessResponse;
import com.nexra.hrms.nexra.modules.auth.entity.UserAccount;
import com.nexra.hrms.nexra.modules.auth.entity.UserProductAccess;
import com.nexra.hrms.nexra.modules.auth.enums.ProductType;
import com.nexra.hrms.nexra.modules.auth.enums.ProductRole;
import com.nexra.hrms.nexra.modules.auth.exception.BusinessException;
import com.nexra.hrms.nexra.modules.auth.exception.ResourceNotFoundException;
import com.nexra.hrms.nexra.modules.auth.repository.UserAccountRepository;
import com.nexra.hrms.nexra.modules.auth.repository.UserProductAccessRepository;
import com.nexra.hrms.nexra.modules.auth.security.JwtPrincipal;
import com.nexra.hrms.nexra.modules.auth.service.ProductAccessService;
import com.nexra.hrms.nexra.modules.auth.service.security.SecurityAuditService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Manages product-level access lifecycle for tenant users across HRMS and CRM products.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ProductAccessServiceImpl implements ProductAccessService {

    private final UserAccountRepository userAccountRepository;
    private final UserProductAccessRepository userProductAccessRepository;
    private final SecurityAuditService securityAuditService;
    private final ModelMapper modelMapper;

    /**
     * Returns all product access grants for a user.
     *
     * @param userId user account identifier
     * @param actor authenticated admin requesting the data
     * @return list of product access responses
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProductAccessResponse> getProductAccess(final UUID userId, final JwtPrincipal actor) {
        log.info("ProductAccessServiceImpl() - getProductAccess() - Fetching product access, userId={}", userId);
        UserAccount user = resolveUser(userId);
        assertCanManageUser(user, actor, "PRODUCT_ACCESS_LIST");
        securityAuditService.record("PRODUCT_ACCESS_LIST", user.getTenant().getCode(), user.getEmail(), "SUCCESS",
            "Product access grants listed by admin.");
        return userProductAccessRepository.findByUser(user).stream()
            .map(access -> {
                ProductAccessResponse response = modelMapper.map(access, ProductAccessResponse.class);
                response.setUserId(user.getId());
                return response;
            })
            .toList();
    }

    /**
     * Grants a user access to a specific product with an assigned role.
     *
     * @param userId target user account identifier
     * @param request product and role assignment payload
     * @param actor authenticated admin performing the grant
     * @return created product access response
     */
    @Override
    @Transactional
    public ProductAccessResponse grantAccess(
        final UUID userId,
        final GrantProductAccessRequest request,
        final JwtPrincipal actor
    ) {
        log.info("ProductAccessServiceImpl() - grantAccess() - Granting product access, userId={}, product={}, role={}",
            userId, request.product(), request.productRole());
        UserAccount user = resolveUser(userId);
        assertCanManageUser(user, actor, "PRODUCT_ACCESS_GRANT");

        if (userProductAccessRepository.existsByUserAndProduct(user, request.product())) {
            securityAuditService.record("PRODUCT_ACCESS_GRANT", user.getTenant().getCode(), user.getEmail(), "FAILURE",
                "Duplicate product access grant rejected for product=" + request.product().name());
            throw new BusinessException("User already has access to product: " + request.product().name());
        }
        assertRoleCompatibleWithProduct(request, user);

        UserProductAccess access = new UserProductAccess();
        access.setUser(user);
        access.setProduct(request.product());
        access.setProductRole(request.productRole());
        access.setGrantedAt(Instant.now());
        access.setGrantedBy(actor != null && actor.userId() != null ? actor.userId().toString() : null);

        UserProductAccess saved = userProductAccessRepository.save(access);
        securityAuditService.record("PRODUCT_ACCESS_GRANT", user.getTenant().getCode(), user.getEmail(), "SUCCESS",
            "Product access granted for product=" + request.product().name() + ", role=" + request.productRole().name());
        log.info("ProductAccessServiceImpl() - grantAccess() - Product access granted, userId={}, product={}",
            userId, request.product());

        ProductAccessResponse response = modelMapper.map(saved, ProductAccessResponse.class);
        response.setUserId(userId);
        return response;
    }

    /**
     * Revokes a user's access to a specific product.
     *
     * @param userId target user account identifier
     * @param product product type to revoke
     * @param actor authenticated admin performing the revoke
     */
    @Override
    @Transactional
    public void revokeAccess(final UUID userId, final ProductType product, final JwtPrincipal actor) {
        log.info("ProductAccessServiceImpl() - revokeAccess() - Revoking product access, userId={}, product={}",
            userId, product);
        UserAccount user = resolveUser(userId);
        assertCanManageUser(user, actor, "PRODUCT_ACCESS_REVOKE");

        if (!userProductAccessRepository.existsByUserAndProduct(user, product)) {
            securityAuditService.record("PRODUCT_ACCESS_REVOKE", user.getTenant().getCode(), user.getEmail(), "FAILURE",
                "Product access revoke rejected because grant was missing for product=" + product.name());
            throw new ResourceNotFoundException("No access grant found for userId=" + userId + " and product=" + product.name());
        }

        userProductAccessRepository.deleteByUserAndProduct(user, product);
        securityAuditService.record("PRODUCT_ACCESS_REVOKE", user.getTenant().getCode(), user.getEmail(), "SUCCESS",
            "Product access revoked for product=" + product.name());
        log.info("ProductAccessServiceImpl() - revokeAccess() - Product access revoked, userId={}, product={}",
            userId, product);
    }

    /**
     * Resolves a user account by identifier or throws resource not found.
     *
     * @param userId user account identifier
     * @return resolved user account
     */
    private UserAccount resolveUser(final UUID userId) {
        return userAccountRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found for id: " + userId));
    }

    private void assertCanManageUser(final UserAccount targetUser, final JwtPrincipal actor, final String eventType) {
        if (actor == null) {
            securityAuditService.record(eventType, targetUser.getTenant().getCode(), targetUser.getEmail(), "FAILURE",
                "Product access action rejected because admin principal was missing.");
            throw new AccessDeniedException("Admin principal is required.");
        }
        if (hasRole(actor, "ROLE_PLATFORM_ADMIN")) {
            return;
        }
        if (hasRole(actor, "ROLE_TENANT_ADMIN")
            && actor.tenantCode() != null
            && actor.tenantCode().equalsIgnoreCase(targetUser.getTenant().getCode())) {
            return;
        }
        securityAuditService.record(eventType, targetUser.getTenant().getCode(), targetUser.getEmail(), "FAILURE",
            "Product access action rejected because admin tenant scope did not match target user.");
        throw new AccessDeniedException("Cannot manage product access outside the authenticated tenant.");
    }

    private boolean hasRole(final JwtPrincipal actor, final String role) {
        return actor.roles() != null && actor.roles().contains(role);
    }

    private void assertRoleCompatibleWithProduct(final GrantProductAccessRequest request, final UserAccount user) {
        if (isRoleCompatibleWithProduct(request.product(), request.productRole())) {
            return;
        }
        securityAuditService.record("PRODUCT_ACCESS_GRANT", user.getTenant().getCode(), user.getEmail(), "FAILURE",
            "Product access grant rejected because role=" + request.productRole().name()
                + " is incompatible with product=" + request.product().name());
        throw new BusinessException("Product role " + request.productRole().name()
            + " is not valid for product " + request.product().name());
    }

    private boolean isRoleCompatibleWithProduct(final ProductType product, final ProductRole role) {
        return switch (role) {
            case TENANT_ADMIN -> true;
            case EMPLOYEE, HR_MANAGER, PAYROLL_ADMIN, FINANCE_ADMIN, RECRUITMENT_ADMIN, PERFORMANCE_ADMIN,
                ONBOARDING_ADMIN, DEPARTMENT_HEAD -> product == ProductType.HRMS;
            case SALES_REP, ACCOUNT_MANAGER, SALES_MANAGER, SUPPORT_AGENT -> product == ProductType.CRM;
        };
    }
}
