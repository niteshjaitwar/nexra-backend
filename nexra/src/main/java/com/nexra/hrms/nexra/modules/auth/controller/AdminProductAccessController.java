package com.nexra.hrms.nexra.modules.auth.controller;

import com.nexra.hrms.nexra.modules.auth.dto.request.GrantProductAccessRequest;
import com.nexra.hrms.nexra.modules.auth.dto.response.ApiResponse;
import com.nexra.hrms.nexra.modules.auth.dto.response.ProductAccessResponse;
import com.nexra.hrms.nexra.modules.auth.enums.ProductType;
import com.nexra.hrms.nexra.modules.auth.security.JwtPrincipal;
import com.nexra.hrms.nexra.modules.auth.service.ProductAccessService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes admin APIs for managing user product access grants across HRMS and CRM.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/users/{userId}/products")
public class AdminProductAccessController {

    private final ProductAccessService productAccessService;

    /**
     * Returns all product access grants for a given user.
     *
     * @param userId target user account identifier
     * @return standardized API response with product access list
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<List<ProductAccessResponse>>> getProductAccess(
        @PathVariable final UUID userId
    ) {
        log.info("AdminProductAccessController() - getProductAccess() - List product access endpoint invoked, userId={}", userId);
        List<ProductAccessResponse> response = productAccessService.getProductAccess(userId);
        return ResponseEntity.ok(ApiResponse.success("Product access fetched successfully.", response));
    }

    /**
     * Grants a user access to a specific product with an assigned role.
     *
     * @param userId target user account identifier
     * @param request product and role assignment payload
     * @param principal authenticated admin principal
     * @return standardized API response with created access grant
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<ProductAccessResponse>> grantProductAccess(
        @PathVariable final UUID userId,
        @Valid @RequestBody final GrantProductAccessRequest request,
        @AuthenticationPrincipal final JwtPrincipal principal
    ) {
        log.info("AdminProductAccessController() - grantProductAccess() - Grant access endpoint invoked, userId={}, product={}, role={}",
            userId, request.product(), request.productRole());
        UUID grantedBy = principal != null ? principal.userId() : null;
        ProductAccessResponse response = productAccessService.grantAccess(userId, request, grantedBy);
        return ResponseEntity.ok(ApiResponse.success("Product access granted successfully.", response));
    }

    /**
     * Revokes a user's access to a specific product.
     *
     * @param userId target user account identifier
     * @param product product type to revoke
     * @return standardized API response
     */
    @DeleteMapping("/{product}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> revokeProductAccess(
        @PathVariable final UUID userId,
        @PathVariable final ProductType product
    ) {
        log.info("AdminProductAccessController() - revokeProductAccess() - Revoke access endpoint invoked, userId={}, product={}",
            userId, product);
        productAccessService.revokeAccess(userId, product);
        return ResponseEntity.ok(ApiResponse.success("Product access revoked successfully.", null));
    }
}
