package com.nexra.hrms.nexra.modules.admin.controller;

import com.nexra.hrms.nexra.common.api.ApiResponse;
import com.nexra.hrms.nexra.common.audit.AuditEventView;
import com.nexra.hrms.nexra.common.exception.NexraForbiddenException;
import com.nexra.hrms.nexra.common.exception.NexraUnauthorizedException;
import com.nexra.hrms.nexra.modules.admin.service.AdminInsightsService;
import com.nexra.hrms.nexra.modules.auth.security.JwtPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Tag(name = "Admin Insights", description = "Tenant admin operational insights and audit endpoints.")
@RestController
@RequestMapping("/api/v1/admin/insights")
@RequiredArgsConstructor
public class AdminInsightsController {

    private final AdminInsightsService adminInsightsService;

    @Operation(summary = "Tenant operational summary", description = "Returns tenant-scoped operational counts across modules.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Admin summary fetched."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Admin permission missing.")
    })
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<Map<String, Long>>> summary() {
        final String tenantCode = resolveTenantCode();
        return ResponseEntity.ok(ApiResponse.ok(adminInsightsService.tenantSummary(tenantCode), "Admin summary fetched successfully."));
    }

    @Operation(summary = "Recent audit events", description = "Returns recent audit events filtered by module when provided.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Audit events fetched."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Admin permission missing.")
    })
    @GetMapping("/audit-events")
    public ResponseEntity<ApiResponse<List<AuditEventView>>> auditEvents(
        @RequestParam(required = false) final String module,
        @RequestParam(defaultValue = "50") final int limit
    ) {
        final String tenantCode = resolveTenantCode();
        return ResponseEntity.ok(
            ApiResponse.ok(
                adminInsightsService.recentAuditEvents(tenantCode, module, limit),
                "Admin audit events fetched successfully."
            )
        );
    }

    private String resolveTenantCode() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof JwtPrincipal principal)) {
            throw new NexraUnauthorizedException("Authentication is required.");
        }
        if (!principal.roles().contains("ROLE_TENANT_ADMIN") && !principal.roles().contains("ROLE_PLATFORM_ADMIN")) {
            throw new NexraForbiddenException("User does not have admin insights permission.");
        }
        if (!StringUtils.hasText(principal.tenantCode())) {
            throw new NexraUnauthorizedException("Authenticated admin user is missing tenant context.");
        }
        return principal.tenantCode().trim();
    }
}

