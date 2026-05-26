package com.nexra.hrms.nexra.modules.admin.controller;

import com.nexra.hrms.nexra.common.api.ApiResponse;
import com.nexra.hrms.nexra.common.audit.AuditEventRecord;
import com.nexra.hrms.nexra.common.audit.AuditEventService;
import com.nexra.hrms.nexra.common.audit.AuditEventView;
import com.nexra.hrms.nexra.common.exception.NexraForbiddenException;
import com.nexra.hrms.nexra.common.exception.NexraUnauthorizedException;
import com.nexra.hrms.nexra.common.exception.NexraValidationException;
import com.nexra.hrms.nexra.modules.admin.service.AdminInsightsService;
import com.nexra.hrms.nexra.modules.auth.security.JwtPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Tag(name = "Admin Insights", description = "Tenant admin operational insights and audit endpoints.")
@RestController
@RequestMapping("/api/v1/admin/insights")
@RequiredArgsConstructor
@Validated
public class AdminInsightsController {

    private static final Set<String> BASE_AUDIT_MODULES = Set.of("ADMIN", "AUTH");
    private static final Set<String> HRMS_AUDIT_MODULES = Set.of(
        "HRMS",
        "ATTENDANCE",
        "EMPLOYEE",
        "EXPENSE",
        "LEAVE",
        "ONBOARDING",
        "PERFORMANCE",
        "RECRUITMENT",
        "TIMESHEET"
    );
    private static final Set<String> PAYROLL_AUDIT_MODULES = Set.of("PAYROLL");
    private static final Set<String> CRM_AUDIT_MODULES = Set.of("CRM");

    private final AdminInsightsService adminInsightsService;
    private final AuditEventService auditEventService;

    @Operation(summary = "Tenant operational summary", description = "Returns tenant-scoped operational counts across modules.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Admin summary fetched."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Admin permission missing.")
    })
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<Map<String, Long>>> summary(
        @AuthenticationPrincipal final JwtPrincipal principal,
        final HttpServletRequest request
    ) {
        final AdminAccessContext context = resolveAccessContext(principal);
        auditAdminRead(context, "ADMIN_SUMMARY_READ", null, request);
        return ResponseEntity.ok(ApiResponse.ok(
            adminInsightsService.tenantSummary(context.tenantCode(), context.products(), context.platformAdmin()),
            "Admin summary fetched successfully."
        ));
    }

    @Operation(summary = "Recent audit events", description = "Returns recent audit events filtered by module when provided.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Audit events fetched."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Admin permission missing.")
    })
    @GetMapping("/audit-events")
    public ResponseEntity<ApiResponse<List<AuditEventView>>> auditEvents(
        @AuthenticationPrincipal final JwtPrincipal principal,
        @RequestParam(required = false)
        @Pattern(regexp = "^[A-Za-z][A-Za-z0-9_-]{1,39}$", message = "module must be 2-40 letters, digits, hyphen, or underscore.")
        final String module,
        @RequestParam(defaultValue = "50") @Min(1) @Max(200) final int limit,
        final HttpServletRequest request
    ) {
        final AdminAccessContext context = resolveAccessContext(principal);
        final String normalizedModule = normalizeModule(module);
        verifyModuleAccess(context, normalizedModule);
        auditAdminRead(context, "ADMIN_AUDIT_EVENTS_READ", normalizedModule, request);
        return ResponseEntity.ok(
            ApiResponse.ok(
                adminInsightsService.recentAuditEvents(context.tenantCode(), normalizedModule, limit),
                "Admin audit events fetched successfully."
            )
        );
    }

    private AdminAccessContext resolveAccessContext(final JwtPrincipal principal) {
        if (principal == null) {
            throw new NexraUnauthorizedException("Authentication is required.");
        }
        final boolean platformAdmin = principal.roles().contains("ROLE_PLATFORM_ADMIN");
        final boolean tenantAdmin = principal.roles().contains("ROLE_TENANT_ADMIN");
        if (!tenantAdmin && !platformAdmin) {
            throw new NexraForbiddenException("User does not have admin insights permission.");
        }
        if (!StringUtils.hasText(principal.tenantCode())) {
            throw new NexraUnauthorizedException("Authenticated admin user is missing tenant context.");
        }
        return new AdminAccessContext(
            principal.tenantCode().trim().toUpperCase(),
            principal.email(),
            principal.userId().toString(),
            normalizeProducts(principal.products()),
            platformAdmin
        );
    }

    private Set<String> normalizeProducts(final Set<String> products) {
        if (products == null) {
            return Set.of();
        }
        return products.stream()
            .filter(StringUtils::hasText)
            .map(product -> product.trim().toUpperCase())
            .collect(Collectors.toUnmodifiableSet());
    }

    private String normalizeModule(final String module) {
        return StringUtils.hasText(module) ? module.trim().toUpperCase().replace('-', '_') : null;
    }

    private void verifyModuleAccess(final AdminAccessContext context, final String module) {
        if (module == null || context.platformAdmin() || BASE_AUDIT_MODULES.contains(module)) {
            return;
        }
        if (context.products().contains("HRMS") && HRMS_AUDIT_MODULES.contains(module)) {
            return;
        }
        if (context.products().contains("PAYROLL") && PAYROLL_AUDIT_MODULES.contains(module)) {
            return;
        }
        if (context.products().contains("CRM") && CRM_AUDIT_MODULES.contains(module)) {
            return;
        }
        throw new NexraForbiddenException("Admin user does not have access to requested module insights.");
    }

    private void auditAdminRead(
        final AdminAccessContext context,
        final String action,
        final String moduleFilter,
        final HttpServletRequest request
    ) {
        auditEventService.record(AuditEventRecord
            .of(context.tenantCode(), "ADMIN", action, "SUCCESS")
            .withActor(context.email(), context.userId())
            .withDetail(moduleFilter == null ? null : "{\"module\":\"" + moduleFilter + "\"}")
            .withRequestInfo(request.getRemoteAddr(), request.getHeader("X-Request-Id")));
    }

    private record AdminAccessContext(
        String tenantCode,
        String email,
        String userId,
        Set<String> products,
        boolean platformAdmin
    ) {
        private AdminAccessContext {
            if (!StringUtils.hasText(tenantCode)) {
                throw new NexraValidationException("Tenant context is required for admin insights.");
            }
        }
    }
}

