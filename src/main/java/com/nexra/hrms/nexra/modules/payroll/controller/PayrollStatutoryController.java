package com.nexra.hrms.nexra.modules.payroll.controller;

import com.nexra.hrms.nexra.common.api.ApiResponse;
import com.nexra.hrms.nexra.modules.payroll.dto.PayrollFilingGenerateRequest;
import com.nexra.hrms.nexra.modules.payroll.dto.PayrollStatutoryFilingSubmitRequest;
import com.nexra.hrms.nexra.modules.payroll.exception.PayrollForbiddenException;
import com.nexra.hrms.nexra.modules.payroll.exception.PayrollUnauthorizedException;
import com.nexra.hrms.nexra.modules.payroll.model.PayrollStatutoryFiling;
import com.nexra.hrms.nexra.modules.payroll.security.AuthenticatedPayrollUser;
import com.nexra.hrms.nexra.modules.payroll.security.PayrollAuthFilter;
import com.nexra.hrms.nexra.modules.payroll.service.PayrollStatutoryFilingService;
import com.nexra.hrms.nexra.modules.payroll.service.PayrollStatutoryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Tenant-scoped statutory preview, breakdown, and filing-artifact endpoints.
 * Secured by the payroll module chain ({@link PayrollAuthFilter}); authorization
 * is enforced against the {@link AuthenticatedPayrollUser} product scope and role.
 */
@RestController
@RequestMapping("/api/v1/payroll/statutory")
@RequiredArgsConstructor
public class PayrollStatutoryController {

    private final PayrollStatutoryService statutoryService;
    private final PayrollStatutoryFilingService filingService;

    @GetMapping("/{countryCode}/preview")
    public ResponseEntity<ApiResponse<Map<String, BigDecimal>>> preview(
        @PathVariable final String countryCode,
        @RequestParam(defaultValue = "0") final BigDecimal grossMonthly,
        final HttpServletRequest httpRequest
    ) {
        final AuthenticatedPayrollUser actor = currentUser(httpRequest);
        return ResponseEntity.ok(ApiResponse.ok(
            statutoryService.calculateStatutory(actor.tenantCode(), countryCode, grossMonthly),
            "Statutory preview calculated successfully."
        ));
    }

    @GetMapping("/{countryCode}/breakdown")
    public ResponseEntity<ApiResponse<PayrollStatutoryService.StatutoryBreakdown>> breakdown(
        @PathVariable final String countryCode,
        @RequestParam(defaultValue = "0") final BigDecimal grossMonthly,
        final HttpServletRequest httpRequest
    ) {
        final AuthenticatedPayrollUser actor = currentUser(httpRequest);
        return ResponseEntity.ok(ApiResponse.ok(
            statutoryService.calculateBreakdown(actor.tenantCode(), countryCode, grossMonthly),
            "Statutory breakdown calculated successfully."
        ));
    }

    @PostMapping("/{countryCode}/filings")
    public ResponseEntity<ApiResponse<PayrollStatutoryFiling>> generateFiling(
        @PathVariable final String countryCode,
        @Valid @RequestBody final PayrollFilingGenerateRequest request,
        final HttpServletRequest httpRequest
    ) {
        final AuthenticatedPayrollUser actor = currentUser(httpRequest);
        requirePayrollManager(actor);
        final PayrollStatutoryFiling filing = filingService.generate(
            actor.tenantCode(),
            countryCode,
            request.period(),
            request.grossAmounts(),
            actor.email()
        );
        return ResponseEntity.ok(ApiResponse.ok(filing, "Statutory filing generated successfully."));
    }

    @GetMapping("/{countryCode}/filings")
    public ResponseEntity<ApiResponse<List<PayrollStatutoryFiling>>> listFilings(
        @PathVariable final String countryCode,
        final HttpServletRequest httpRequest
    ) {
        final AuthenticatedPayrollUser actor = currentUser(httpRequest);
        return ResponseEntity.ok(ApiResponse.ok(
            filingService.list(actor.tenantCode(), countryCode),
            "Statutory filings fetched successfully."
        ));
    }

    @GetMapping("/{countryCode}/filings/{filingId}")
    public ResponseEntity<ApiResponse<PayrollStatutoryFiling>> getFiling(
        @PathVariable final String countryCode,
        @PathVariable final String filingId,
        final HttpServletRequest httpRequest
    ) {
        final AuthenticatedPayrollUser actor = currentUser(httpRequest);
        return ResponseEntity.ok(ApiResponse.ok(
            filingService.findById(actor.tenantCode(), filingId),
            "Statutory filing fetched successfully."
        ));
    }

    @PostMapping("/{countryCode}/filings/{filingId}/submit")
    public ResponseEntity<ApiResponse<PayrollStatutoryFiling>> submitFiling(
        @PathVariable final String countryCode,
        @PathVariable final String filingId,
        @Valid @RequestBody(required = false) final PayrollStatutoryFilingSubmitRequest request,
        final HttpServletRequest httpRequest
    ) {
        final AuthenticatedPayrollUser actor = currentUser(httpRequest);
        requirePayrollManager(actor);
        final String reference = request == null ? null : request.submissionReference();
        return ResponseEntity.ok(ApiResponse.ok(
            filingService.submit(actor.tenantCode(), filingId, reference, actor.email()),
            "Statutory filing submitted successfully."
        ));
    }

    @PostMapping("/{countryCode}/filings/{filingId}/lock")
    public ResponseEntity<ApiResponse<PayrollStatutoryFiling>> lockFiling(
        @PathVariable final String countryCode,
        @PathVariable final String filingId,
        final HttpServletRequest httpRequest
    ) {
        final AuthenticatedPayrollUser actor = currentUser(httpRequest);
        requirePayrollManager(actor);
        return ResponseEntity.ok(ApiResponse.ok(
            filingService.lock(actor.tenantCode(), filingId, actor.email()),
            "Statutory filing locked successfully."
        ));
    }

    @GetMapping("/{countryCode}/filings/{filingId}/export")
    public ResponseEntity<?> exportFiling(
        @PathVariable final String countryCode,
        @PathVariable final String filingId,
        @RequestParam(defaultValue = "json") final String format,
        final HttpServletRequest httpRequest
    ) {
        final AuthenticatedPayrollUser actor = currentUser(httpRequest);
        requirePayrollManager(actor);
        if ("xml".equalsIgnoreCase(format)) {
            return ResponseEntity.ok(ApiResponse.ok(
                filingService.buildExportXml(actor.tenantCode(), filingId),
                "Statutory filing export XML generated successfully."
            ));
        }
        return ResponseEntity.ok(ApiResponse.ok(
            filingService.buildExportPayload(actor.tenantCode(), filingId),
            "Statutory filing export payload generated successfully."
        ));
    }

    private AuthenticatedPayrollUser currentUser(final HttpServletRequest request) {
        final Object value = request.getAttribute(PayrollAuthFilter.ATTR_AUTH_USER);
        if (value instanceof AuthenticatedPayrollUser user) {
            if (!user.products().contains("PAYROLL") && !user.products().contains("HRMS")) {
                throw new PayrollForbiddenException("User does not have payroll product access");
            }
            return user;
        }
        throw new PayrollUnauthorizedException("Missing authenticated payroll user");
    }

    private void requirePayrollManager(final AuthenticatedPayrollUser actor) {
        if (hasRole(actor, "PLATFORM_ADMIN") || hasRole(actor, "HR_ADMIN") || hasRole(actor, "PAYROLL_ADMIN")) {
            return;
        }
        throw new PayrollForbiddenException("User does not have payroll administration permission");
    }

    private boolean hasRole(final AuthenticatedPayrollUser actor, final String role) {
        return actor.roles().contains(role) || actor.roles().contains("ROLE_" + role);
    }
}
