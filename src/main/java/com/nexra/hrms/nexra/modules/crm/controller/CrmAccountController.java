package com.nexra.hrms.nexra.modules.crm.controller;

import com.nexra.hrms.nexra.common.api.ApiResponse;
import com.nexra.hrms.nexra.common.api.PageResponse;
import com.nexra.hrms.nexra.common.exception.NexraForbiddenException;
import com.nexra.hrms.nexra.common.exception.NexraUnauthorizedException;
import com.nexra.hrms.nexra.modules.auth.security.JwtPrincipal;
import com.nexra.hrms.nexra.modules.crm.config.CrmProperties;
import com.nexra.hrms.nexra.modules.crm.dto.request.CrmAccountCreateRequest;
import com.nexra.hrms.nexra.modules.crm.dto.request.CrmAccountUpdateRequest;
import com.nexra.hrms.nexra.modules.crm.model.CrmAccount;
import com.nexra.hrms.nexra.modules.crm.service.CrmAccountService;
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

@Tag(name = "CRM Accounts", description = "Account management APIs for CRM module.")
@RestController
@RequestMapping("/api/v1/crm/accounts")
@RequiredArgsConstructor
public class CrmAccountController {

    private final CrmAccountService service;
    private final CrmProperties properties;

    @Operation(summary = "Create CRM account")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "CRM account created."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid account payload."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "CRM product access missing.")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<CrmAccount>> create(@Valid @RequestBody final CrmAccountCreateRequest request) {
        return ResponseEntity.status(201).body(ApiResponse.created(service.create(resolveTenantCode(), request), "CRM account created successfully."));
    }

    @Operation(summary = "Get CRM account")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "CRM account fetched."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "CRM product access missing."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "CRM account not found.")
    })
    @GetMapping("/{accountId}")
    public ResponseEntity<ApiResponse<CrmAccount>> getById(@PathVariable final String accountId) {
        return ResponseEntity.ok(ApiResponse.ok(service.findById(resolveTenantCode(), accountId), "CRM account fetched successfully."));
    }

    @Operation(summary = "List CRM accounts")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "CRM accounts listed."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "CRM product access missing."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Invalid pagination parameters.")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CrmAccount>>> list(
        @RequestParam(defaultValue = "0") final int page,
        @RequestParam(defaultValue = "20") final int size
    ) {
        return ResponseEntity.ok(ApiResponse.ok(service.list(resolveTenantCode(), page, size), "CRM accounts listed successfully."));
    }

    @Operation(summary = "Update CRM account")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "CRM account updated."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid account payload."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "CRM product access missing."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "CRM account not found.")
    })
    @PutMapping("/{accountId}")
    public ResponseEntity<ApiResponse<CrmAccount>> update(
        @PathVariable final String accountId,
        @Valid @RequestBody final CrmAccountUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(service.update(resolveTenantCode(), accountId, request), "CRM account updated successfully."));
    }

    @Operation(summary = "Delete CRM account")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "CRM account deleted."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "CRM product access missing."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "CRM account not found.")
    })
    @DeleteMapping("/{accountId}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable final String accountId) {
        service.delete(resolveTenantCode(), accountId);
        return ResponseEntity.ok(ApiResponse.empty("CRM account deleted successfully."));
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

