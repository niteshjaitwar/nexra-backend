package com.nexra.hrms.nexra.modules.auth.service;

import com.nexra.hrms.nexra.modules.auth.dto.request.GrantProductAccessRequest;
import com.nexra.hrms.nexra.modules.auth.dto.response.ProductAccessResponse;
import com.nexra.hrms.nexra.modules.auth.enums.ProductType;
import com.nexra.hrms.nexra.modules.auth.security.JwtPrincipal;
import java.util.List;
import java.util.UUID;

/**
 * Manages product-level access grants for tenant users across HRMS and CRM products.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
public interface ProductAccessService {

    /**
     * Returns all product access grants for a user.
     *
     * @param userId user account identifier
     * @param actor authenticated admin requesting the data
     * @return list of product access responses
     */
    List<ProductAccessResponse> getProductAccess(UUID userId, JwtPrincipal actor);

    /**
     * Grants a user access to a specific product with an assigned role.
     *
     * @param userId target user account identifier
     * @param request product and role assignment payload
     * @param actor authenticated admin performing the grant
     * @return created product access response
     */
    ProductAccessResponse grantAccess(UUID userId, GrantProductAccessRequest request, JwtPrincipal actor);

    /**
     * Revokes a user's access to a specific product.
     *
     * @param userId target user account identifier
     * @param product product type to revoke
     * @param actor authenticated admin performing the revoke
     */
    void revokeAccess(UUID userId, ProductType product, JwtPrincipal actor);
}
