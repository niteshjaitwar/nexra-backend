package com.nexra.hrms.nexra.modules.auth.controller;

import com.nexra.hrms.nexra.modules.auth.dto.request.GrantProductAccessRequest;
import com.nexra.hrms.nexra.common.api.ApiResponse;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Exposes admin APIs for managing user product access grants across HRMS and CRM.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@Slf4j
@Tag(name = "Authentication", description = "Authentication and identity APIs.")
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
    @Operation(summary = "GET endpoint", description = "Handles GET requests for this resource.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<List<ProductAccessResponse>>> getProductAccess(
        @PathVariable final UUID userId,
        @AuthenticationPrincipal final JwtPrincipal principal
    ) {
        log.info("AdminProductAccessController() - getProductAccess() - List product access endpoint invoked, userId={}", userId);
        List<ProductAccessResponse> response = productAccessService.getProductAccess(userId, principal);
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
    @Operation(summary = "POST endpoint", description = "Handles POST requests for this resource.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<ProductAccessResponse>> grantProductAccess(
        @PathVariable final UUID userId,
        @Valid @RequestBody final GrantProductAccessRequest request,
        @AuthenticationPrincipal final JwtPrincipal principal
    ) {
        log.info("AdminProductAccessController() - grantProductAccess() - Grant access endpoint invoked, userId={}, product={}, role={}",
            userId, request.product(), request.productRole());
        ProductAccessResponse response = productAccessService.grantAccess(userId, request, principal);
        return ResponseEntity.ok(ApiResponse.success("Product access granted successfully.", response));
    }

    /**
     * Revokes a user's access to a specific product.
     *
     * @param userId target user account identifier
     * @param product product type to revoke
     * @return standardized API response
     */
    @Operation(summary = "DELETE endpoint", description = "Handles DELETE requests for this resource.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
    @DeleteMapping("/{product}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> revokeProductAccess(
        @PathVariable final UUID userId,
        @PathVariable final ProductType product,
        @AuthenticationPrincipal final JwtPrincipal principal
    ) {
        log.info("AdminProductAccessController() - revokeProductAccess() - Revoke access endpoint invoked, userId={}, product={}",
            userId, product);
        productAccessService.revokeAccess(userId, product, principal);
        return ResponseEntity.ok(ApiResponse.success("Product access revoked successfully.", null));
    }
}
