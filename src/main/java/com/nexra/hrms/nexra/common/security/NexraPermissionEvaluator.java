package com.nexra.hrms.nexra.common.security;

import com.nexra.hrms.nexra.modules.auth.security.JwtPrincipal;
import java.io.Serializable;
import java.util.Set;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Evaluates Nexra fine-grained permissions for method security.
 *
 * <p>Authorization is granted when the principal either holds a global admin role,
 * a domain-scoped admin role for the permission's product, or the product
 * entitlement for the permission. Domain-scoped admin roles deliberately do NOT
 * grant access outside their own product to prevent privilege escalation
 * (for example a CRM admin must not be able to mutate payroll data).
 *
 * @author niteshjaitwar
 */
@Component
public class NexraPermissionEvaluator implements PermissionEvaluator {

    private static final String ROLE_PLATFORM_ADMIN = "ROLE_PLATFORM_ADMIN";
    private static final String ROLE_TENANT_ADMIN = "ROLE_TENANT_ADMIN";
    private static final String ROLE_CRM_ADMIN = "ROLE_CRM_ADMIN";
    private static final String ROLE_HR_ADMIN = "ROLE_HR_ADMIN";

    private static final Set<String> GLOBAL_ADMIN_ROLES = Set.of(ROLE_PLATFORM_ADMIN, ROLE_TENANT_ADMIN);

    @Override
    public boolean hasPermission(final Authentication authentication, final Object targetDomainObject, final Object permission) {
        return hasPermission(authentication, null, null, permission);
    }

    @Override
    public boolean hasPermission(
        final Authentication authentication,
        final Serializable targetId,
        final String targetType,
        final Object permission
    ) {
        if (authentication == null || !(authentication.getPrincipal() instanceof JwtPrincipal principal)) {
            return false;
        }
        if (permission == null) {
            return false;
        }
        final Set<String> roles = principal.roles();
        if (roles != null && roles.stream().anyMatch(GLOBAL_ADMIN_ROLES::contains)) {
            return true;
        }
        final String required = permission.toString();
        return switch (required) {
            case NexraPermission.CRM_READ, NexraPermission.CRM_WRITE ->
                hasRole(roles, ROLE_CRM_ADMIN) || hasProduct(principal, "CRM");
            case NexraPermission.HRMS_READ, NexraPermission.HRMS_WRITE ->
                hasRole(roles, ROLE_HR_ADMIN) || hasProduct(principal, "HRMS");
            case NexraPermission.PAYROLL_READ, NexraPermission.PAYROLL_WRITE ->
                hasRole(roles, ROLE_HR_ADMIN) || hasProduct(principal, "PAYROLL") || hasProduct(principal, "HRMS");
            case NexraPermission.OPS_READ, NexraPermission.OPS_WRITE ->
                hasRole(roles, ROLE_HR_ADMIN) || hasProduct(principal, "OPS") || hasProduct(principal, "HRMS");
            case NexraPermission.WORKFLOW_READ, NexraPermission.WORKFLOW_WRITE ->
                hasRole(roles, ROLE_CRM_ADMIN) || hasRole(roles, ROLE_HR_ADMIN)
                    || hasProduct(principal, "CRM") || hasProduct(principal, "HRMS")
                    || hasProduct(principal, "OPS") || hasProduct(principal, "PAYROLL");
            default -> false;
        };
    }

    private boolean hasRole(final Set<String> roles, final String role) {
        return roles != null && roles.contains(role);
    }

    private boolean hasProduct(final JwtPrincipal principal, final String product) {
        return principal.products() != null && principal.products().contains(product);
    }
}
