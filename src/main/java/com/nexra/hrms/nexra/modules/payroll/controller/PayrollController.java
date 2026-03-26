package com.nexra.hrms.nexra.modules.payroll.controller;

import com.nexra.hrms.nexra.modules.payroll.dto.ApiResponse;
import com.nexra.hrms.nexra.modules.payroll.dto.PayrollGenerationRequest;
import com.nexra.hrms.nexra.modules.payroll.dto.ProfilePayrollGenerationRequest;
import com.nexra.hrms.nexra.modules.payroll.exception.PayrollBusinessException;
import com.nexra.hrms.nexra.modules.payroll.exception.PayrollForbiddenException;
import com.nexra.hrms.nexra.modules.payroll.exception.PayrollUnauthorizedException;
import com.nexra.hrms.nexra.modules.payroll.model.AuthDependencyStatus;
import com.nexra.hrms.nexra.modules.payroll.model.PayrollSlip;
import com.nexra.hrms.nexra.modules.payroll.security.AuthenticatedPayrollUser;
import com.nexra.hrms.nexra.modules.payroll.security.PayrollAuthFilter;
import com.nexra.hrms.nexra.modules.payroll.service.PayrollService;
import com.nexra.hrms.nexra.modules.payroll.service.PayslipDocumentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.validation.annotation.Validated;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1/payroll")
@Slf4j
@Validated
public class PayrollController {

    private final PayrollService payrollService;
    private final PayslipDocumentService payslipDocumentService;

    @GetMapping("/status")
    @ResponseBody
    public ResponseEntity<ApiResponse<Map<String, Object>>> status() {
        return ResponseEntity.ok(ApiResponse.success(
            "payroll service is available.",
            Map.of(
                "service", "payroll",
                "state", "UP",
                "storage", "In-memory reference engine",
                "security", "JWT tenant-scoped"
            )
        ));
    }

    @GetMapping("/capabilities")
    @ResponseBody
    public ResponseEntity<ApiResponse<Map<String, Object>>> capabilities() {
        return ResponseEntity.ok(ApiResponse.success(
            "Payroll capabilities fetched successfully.",
            Map.of(
                "domains", List.of("organization-profile", "employee-profile", "payroll-generation", "payslip-documents"),
                "documents", List.of("html", "pdf"),
                "dependencyMode", "Auth module in-process"
            )
        ));
    }

    @PostMapping("/generate")
    @ResponseBody
    public ResponseEntity<ApiResponse<Map<String, Object>>> generatePayroll(
        @Valid @RequestBody final PayrollGenerationRequest request,
        final HttpServletRequest httpRequest
    ) {
        AuthenticatedPayrollUser actor = currentUser(httpRequest);
        requirePayrollManager(actor);
        log.info("PayrollController - generatePayroll - tenantCode={}, employeeId={}, payPeriod={}",
            request.tenantCode(), request.employeeId(), request.payPeriod());
        PayrollSlip slip = payrollService.generatePayroll(request, actor);
        return ResponseEntity.ok(ApiResponse.success("Payroll generated successfully.", buildSlipLinksPayload(slip)));
    }

    @PostMapping("/generate/from-profile")
    @ResponseBody
    public ResponseEntity<ApiResponse<Map<String, Object>>> generatePayrollFromProfile(
        @Valid @RequestBody final ProfilePayrollGenerationRequest request,
        final HttpServletRequest httpRequest
    ) {
        AuthenticatedPayrollUser actor = currentUser(httpRequest);
        requirePayrollManager(actor);
        log.info("PayrollController - generatePayrollFromProfile - tenantCode={}, employeeId={}, payPeriod={}",
            request.tenantCode(), request.employeeId(), request.payPeriod());
        PayrollSlip slip = payrollService.generatePayrollFromProfile(request, actor);
        return ResponseEntity.ok(ApiResponse.success("Payroll generated from employee/org profile.", buildSlipLinksPayload(slip)));
    }

    @GetMapping("/{slipId}")
    @ResponseBody
    public ResponseEntity<ApiResponse<PayrollSlip>> getPayroll(
        @PathVariable @NotBlank @Size(max = 64) final String slipId,
        final HttpServletRequest httpRequest
    ) {
        AuthenticatedPayrollUser actor = currentUser(httpRequest);
        PayrollSlip slip = tenantOwnedSlip(slipId, actor);
        return ResponseEntity.ok(ApiResponse.success("Payroll fetched successfully.", slip));
    }

    @GetMapping
    @ResponseBody
    public ResponseEntity<ApiResponse<List<PayrollSlip>>> listPayrolls(
        @RequestParam @NotBlank @Size(max = 60) final String tenantCode,
        final HttpServletRequest httpRequest
    ) {
        AuthenticatedPayrollUser actor = currentUser(httpRequest);
        return ResponseEntity.ok(ApiResponse.success(
            "Payroll slips fetched successfully.",
            payrollService.listSlipsForTenant(tenantCode, actor)
        ));
    }

    @GetMapping(value = "/payslips/{slipId}/html", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String renderPayslipHtml(
        @PathVariable @NotBlank @Size(max = 64) final String slipId,
        final HttpServletRequest httpRequest
    ) {
        AuthenticatedPayrollUser actor = currentUser(httpRequest);
        PayrollSlip slip = tenantOwnedSlip(slipId, actor);
        return payslipDocumentService.renderPayslipHtml(slip);
    }

    @GetMapping(value = "/payslips/{slipId}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @ResponseBody
    public ResponseEntity<byte[]> downloadPayslipPdf(
        @PathVariable @NotBlank @Size(max = 64) final String slipId,
        final HttpServletRequest httpRequest
    ) {
        AuthenticatedPayrollUser actor = currentUser(httpRequest);
        PayrollSlip slip = tenantOwnedSlip(slipId, actor);
        byte[] pdf = payslipDocumentService.generateProtectedPdf(slip);

        String filename = ("payslip-" + slip.employeeCode() + "-" + slip.payPeriod() + ".pdf")
            .replaceAll("[^a-zA-Z0-9._-]", "_");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment().filename(filename, StandardCharsets.UTF_8).build());
        headers.setCacheControl("no-store, no-cache, must-revalidate, max-age=0");
        headers.add("Pragma", "no-cache");
        headers.add("X-Content-Type-Options", "nosniff");

        return ResponseEntity.ok().headers(headers).body(pdf);
    }

    @GetMapping("/dependencies/auth")
    @ResponseBody
    public ResponseEntity<ApiResponse<AuthDependencyStatus>> authDependencyStatus(final HttpServletRequest httpRequest) {
        currentUser(httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Auth service dependency status.", payrollService.getAuthDependencyStatus()));
    }

    private PayrollSlip tenantOwnedSlip(final String slipId, final AuthenticatedPayrollUser actor) {
        PayrollSlip slip = payrollService.getSlip(slipId);
        if (!slip.tenantCode().equalsIgnoreCase(actor.tenantCode())) {
            throw new PayrollBusinessException("Payroll slip does not belong to token tenant");
        }
        return slip;
    }

    private Map<String, Object> buildSlipLinksPayload(final PayrollSlip slip) {
        return Map.of(
            "slip", slip,
            "payslipHtmlUrl", "/api/v1/payroll/payslips/" + slip.slipId() + "/html",
            "payslipPdfUrl", "/api/v1/payroll/payslips/" + slip.slipId() + "/pdf"
        );
    }

    private AuthenticatedPayrollUser currentUser(final HttpServletRequest request) {
        Object value = request.getAttribute(PayrollAuthFilter.ATTR_AUTH_USER);
        if (value instanceof AuthenticatedPayrollUser user) {
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
