package com.nexra.hrms.nexra.modules.auth.security;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Represents JWT-authenticated principal information used by security context.
 * Includes product-scope claims for multi-product HRMS and CRM access control.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
public record JwtPrincipal(
    UUID userId,
    String tenantCode,
    String email,
    Set<String> roles,
    Set<String> products,
    Map<String, String> productRoles
) {
}
