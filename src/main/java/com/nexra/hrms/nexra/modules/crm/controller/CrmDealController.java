package com.nexra.hrms.nexra.modules.crm.controller;

import com.nexra.hrms.nexra.common.api.ApiResponse;
import com.nexra.hrms.nexra.common.api.PageResponse;
import com.nexra.hrms.nexra.common.exception.NexraForbiddenException;
import com.nexra.hrms.nexra.common.exception.NexraUnauthorizedException;
import com.nexra.hrms.nexra.modules.auth.security.JwtPrincipal;
import com.nexra.hrms.nexra.modules.crm.config.CrmProperties;
import com.nexra.hrms.nexra.modules.crm.dto.request.CrmDealCreateRequest;
import com.nexra.hrms.nexra.modules.crm.dto.request.CrmDealUpdateRequest;
import com.nexra.hrms.nexra.modules.crm.model.CrmDeal;
import com.nexra.hrms.nexra.modules.crm.service.CrmDealService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "CRM Deals", description = "Deal management APIs for CRM module.")
@RestController
@RequestMapping("/api/v1/crm/deals")
@RequiredArgsConstructor
public class CrmDealController {

    private final CrmDealService service;
    private final CrmProperties properties;

    @Operation(summary = "Create CRM deal")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "CRM deal created."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid deal payload."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "CRM product access missing.")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<CrmDeal>> create(@Valid @RequestBody final CrmDealCreateRequest request) {
        return ResponseEntity.status(201).body(ApiResponse.created(service.create(resolveTenantCode(), request), "CRM deal created successfully."));
    }

    @Operation(summary = "Get CRM deal")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "CRM deal fetched."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "CRM product access missing."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "CRM deal not found.")
    })
    @GetMapping("/{dealId}")
    public ResponseEntity<ApiResponse<CrmDeal>> getById(@PathVariable final String dealId) {
        return ResponseEntity.ok(ApiResponse.ok(service.findById(resolveTenantCode(), dealId), "CRM deal fetched successfully."));
    }

    @Operation(summary = "List CRM deals")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "CRM deals listed."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "CRM product access missing."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Invalid pagination parameters.")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CrmDeal>>> list(
        @RequestParam(defaultValue = "0") final int page,
        @RequestParam(defaultValue = "20") final int size
    ) {
        return ResponseEntity.ok(ApiResponse.ok(service.list(resolveTenantCode(), page, size), "CRM deals listed successfully."));
    }

    @Operation(summary = "Update CRM deal")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "CRM deal updated."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid deal payload."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "CRM product access missing."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "CRM deal not found.")
    })
    @PutMapping("/{dealId}")
    public ResponseEntity<ApiResponse<CrmDeal>> update(
        @PathVariable final String dealId,
        @Valid @RequestBody final CrmDealUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(service.update(resolveTenantCode(), dealId, request), "CRM deal updated successfully."));
    }

    @Operation(summary = "Delete CRM deal")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "CRM deal deleted."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "CRM product access missing."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "CRM deal not found.")
    })
    @DeleteMapping("/{dealId}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable final String dealId) {
        service.delete(resolveTenantCode(), dealId);
        return ResponseEntity.ok(ApiResponse.empty("CRM deal deleted successfully."));
    }

    private String resolveTenantCode() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof JwtPrincipal principal) {
            requireCrmProductScope(principal);
            if (!StringUtils.hasText(principal.tenantCode())) {
                throw new NexraUnauthorizedException("Authenticated CRM user is missing tenant context.");
            }
            return principal.tenantCode().trim();
        }
        if (!properties.isEnforceAuth()) {
            return "DEV";
        }
        throw new NexraUnauthorizedException("Authentication is required.");
    }

    private void requireCrmProductScope(final JwtPrincipal principal) {
        if (principal.products().contains("CRM")) {
            return;
        }
        throw new NexraForbiddenException("User does not have CRM product access.");
    }
}

