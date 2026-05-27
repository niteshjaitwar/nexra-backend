package com.nexra.hrms.nexra.modules.crm.support;

import com.nexra.hrms.nexra.common.exception.NexraForbiddenException;
import com.nexra.hrms.nexra.common.exception.NexraUnauthorizedException;
import com.nexra.hrms.nexra.modules.auth.security.JwtPrincipal;
import com.nexra.hrms.nexra.modules.crm.config.CrmProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Resolves authenticated CRM request context (principal and tenant) with
 * consistent fallback behavior for non-production/dev-only flows.
 */
@Slf4j
@Component
public class CrmRequestContextResolver {

    private static final String DEV_TENANT_CODE = "DEV";

    /**
     * Resolves tenant for CRM endpoints that allow dev fallback when auth is disabled.
     *
     * @param properties CRM module properties.
     * @return resolved tenant code.
     */
    public String resolveTenantCode(final CrmProperties properties) {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof JwtPrincipal principal) {
            requireCrmProductScope(principal);
            if (!StringUtils.hasText(principal.tenantCode())) {
                throw new NexraUnauthorizedException("Authenticated CRM user is missing tenant context.");
            }
            return principal.tenantCode().trim();
        }
        if (!properties.isEnforceAuth()) {
            return DEV_TENANT_CODE;
        }
        throw new NexraUnauthorizedException("Authentication is required.");
    }

    /**
     * Resolves authenticated principal for admin endpoints that always require auth.
     *
     * @param properties CRM module properties.
     * @return authenticated principal.
     */
    public JwtPrincipal resolveAuthenticatedPrincipal(final CrmProperties properties) {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof JwtPrincipal principal) {
            if (!StringUtils.hasText(principal.tenantCode())) {
                throw new NexraUnauthorizedException("Authenticated CRM user is missing tenant context.");
            }
            return principal;
        }
        if (!properties.isEnforceAuth()) {
            throw new NexraUnauthorizedException("CRM administration requires authentication.");
        }
        throw new NexraUnauthorizedException("Authentication is required.");
    }

    /**
     * Resolves lead access scope. Privileged users can access all tenant
     * records; non-privileged users are owner-scoped.
     *
     * @param properties CRM module properties.
     * @return access scope.
     */
    public CrmAccessScope resolveLeadAccessScope(final CrmProperties properties) {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof JwtPrincipal principal) {
            requireCrmProductScope(principal);
            return new CrmAccessScope(
                principal.userId() == null ? null : principal.userId().toString(),
                isPrivilegedCrmUser(principal)
            );
        }
        if (!properties.isEnforceAuth()) {
            return new CrmAccessScope("DEV", true);
        }
        throw new NexraUnauthorizedException("Authentication is required.");
    }

    /**
     * Resolves generic CRM record access scope.
     *
     * @param properties CRM module properties.
     * @return access scope.
     */
    public CrmAccessScope resolveCrmAccessScope(final CrmProperties properties) {
        return resolveLeadAccessScope(properties);
    }

    /**
     * Enforces CRM product access.
     *
     * @param principal authenticated principal.
     */
    public void requireCrmProductScope(final JwtPrincipal principal) {
        if (principal.products().contains("CRM")) {
            return;
        }
        throw new NexraForbiddenException("User does not have CRM product access.");
    }

    private boolean isPrivilegedCrmUser(final JwtPrincipal principal) {
        final String productRole = principal.productRoles().get("CRM");
        return principal.roles().contains("ROLE_PLATFORM_ADMIN")
            || principal.roles().contains("ROLE_CRM_ADMIN")
            || "TENANT_ADMIN".equals(productRole)
            || "SALES_MANAGER".equals(productRole);
    }
}
