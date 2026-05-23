package com.nexra.hrms.nexra.modules.payroll.controller;

import com.nexra.hrms.nexra.common.api.ApiResponse;
import com.nexra.hrms.nexra.modules.payroll.dto.EmployeeProfileUpsertRequest;
import com.nexra.hrms.nexra.modules.payroll.dto.OrganizationProfileUpsertRequest;
import com.nexra.hrms.nexra.modules.payroll.exception.PayrollForbiddenException;
import com.nexra.hrms.nexra.modules.payroll.exception.PayrollUnauthorizedException;
import com.nexra.hrms.nexra.modules.payroll.model.EmployeeProfile;
import com.nexra.hrms.nexra.modules.payroll.model.OrganizationProfile;
import com.nexra.hrms.nexra.modules.payroll.security.AuthenticatedPayrollUser;
import com.nexra.hrms.nexra.modules.payroll.security.PayrollAuthFilter;
import com.nexra.hrms.nexra.modules.payroll.service.ProfileDirectoryService;
import com.nexra.hrms.nexra.modules.payroll.service.TenantBrandingAssetService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Exposes payroll profile APIs for org-level payroll defaults and employee payroll profiles.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@Tag(name = "Payroll", description = "Payroll APIs.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payroll")
@Slf4j
@Validated
public class PayrollProfileController {

    private final ProfileDirectoryService profileDirectoryService;
    private final TenantBrandingAssetService tenantBrandingAssetService;

    @Operation(summary = "PUT /api/v1/payroll/organization-profile", description = "Processes PUT requests for /api/v1/payroll/organization-profile.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
    @PutMapping("/organization-profile")
    public ResponseEntity<ApiResponse<OrganizationProfile>> upsertOrganizationProfile(
        @Valid @RequestBody final OrganizationProfileUpsertRequest request,
        final HttpServletRequest httpRequest
    ) {
        AuthenticatedPayrollUser actor = currentUser(httpRequest);
        requirePayrollManager(actor);
        log.info("PayrollProfileController - upsertOrganizationProfile - tenantCode={}", request.tenantCode());
        return ResponseEntity.ok(ApiResponse.success(
            "Organization profile saved successfully.",
            profileDirectoryService.upsertOrganizationProfile(request, actor)
        ));
    }

    @Operation(summary = "GET /api/v1/payroll/organization-profile", description = "Processes GET requests for /api/v1/payroll/organization-profile.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
    @GetMapping("/organization-profile")
    public ResponseEntity<ApiResponse<OrganizationProfile>> getOrganizationProfile(
        @RequestParam @NotBlank @Size(max = 60) final String tenantCode,
        final HttpServletRequest httpRequest
    ) {
        AuthenticatedPayrollUser actor = currentUser(httpRequest);
        return ResponseEntity.ok(ApiResponse.success(
            "Organization profile fetched successfully.",
            profileDirectoryService.getOrganizationProfile(tenantCode, actor)
        ));
    }

    @Operation(summary = "POST /api/v1/payroll/organization-profile/logo", description = "Processes POST requests for /api/v1/payroll/organization-profile/logo.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
    @PostMapping("/organization-profile/logo")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadOrganizationLogo(
        @RequestParam @NotBlank @Size(max = 60) final String tenantCode,
        @RequestParam("logoFile") final MultipartFile logoFile,
        final HttpServletRequest httpRequest
    ) {
        AuthenticatedPayrollUser actor = currentUser(httpRequest);
        requirePayrollManager(actor);
        ensureTenantAccess(tenantCode, actor);
        String logoPath = tenantBrandingAssetService.storeTenantLogo(tenantCode, logoFile);
        profileDirectoryService.updateOrganizationBrandingLogoPath(tenantCode, logoPath, actor);
        Map<String, String> payload = Map.of("tenantCode", tenantCode, "brandingLogoPath", logoPath);
        log.info("PayrollProfileController - uploadOrganizationLogo - tenantCode={}", tenantCode);
        return ResponseEntity.ok(ApiResponse.success("Organization logo uploaded successfully.", payload));
    }

    @Operation(summary = "PUT /api/v1/payroll/employees", description = "Processes PUT requests for /api/v1/payroll/employees.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
    @PutMapping("/employees")
    public ResponseEntity<ApiResponse<EmployeeProfile>> upsertEmployeeProfile(
        @Valid @RequestBody final EmployeeProfileUpsertRequest request,
        final HttpServletRequest httpRequest
    ) {
        AuthenticatedPayrollUser actor = currentUser(httpRequest);
        requirePayrollManager(actor);
        log.info("PayrollProfileController - upsertEmployeeProfile - tenantCode={}, employeeId={}",
            request.tenantCode(), request.employeeId());
        return ResponseEntity.ok(ApiResponse.success(
            "Employee profile saved successfully.",
            profileDirectoryService.upsertEmployeeProfile(request, actor)
        ));
    }

    @Operation(summary = "POST /api/v1/payroll/employees", description = "Processes POST requests for /api/v1/payroll/employees.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
    @PostMapping("/employees")
    public ResponseEntity<ApiResponse<EmployeeProfile>> createEmployeeProfile(
        @Valid @RequestBody final EmployeeProfileUpsertRequest request,
        final HttpServletRequest httpRequest
    ) {
        return upsertEmployeeProfile(request, httpRequest);
    }

    @Operation(summary = "GET /api/v1/payroll/employees", description = "Processes GET requests for /api/v1/payroll/employees.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
    @GetMapping("/employees")
    public ResponseEntity<ApiResponse<List<EmployeeProfile>>> listEmployees(
        @RequestParam @NotBlank @Size(max = 60) final String tenantCode,
        final HttpServletRequest httpRequest
    ) {
        AuthenticatedPayrollUser actor = currentUser(httpRequest);
        return ResponseEntity.ok(ApiResponse.success(
            "Employee profiles fetched successfully.",
            profileDirectoryService.listEmployeeProfiles(tenantCode, actor)
        ));
    }

    @Operation(summary = "GET /api/v1/payroll/employees/{employeeId}", description = "Processes GET requests for /api/v1/payroll/employees/{employeeId}.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
    @GetMapping("/employees/{employeeId}")
    public ResponseEntity<ApiResponse<EmployeeProfile>> getEmployee(
        @PathVariable @NotBlank @Size(max = 64) final String employeeId,
        @RequestParam @NotBlank @Size(max = 60) final String tenantCode,
        final HttpServletRequest httpRequest
    ) {
        AuthenticatedPayrollUser actor = currentUser(httpRequest);
        return ResponseEntity.ok(ApiResponse.success(
            "Employee profile fetched successfully.",
            profileDirectoryService.getEmployeeProfile(tenantCode, employeeId, actor)
        ));
    }

    private AuthenticatedPayrollUser currentUser(final HttpServletRequest request) {
        Object value = request.getAttribute(PayrollAuthFilter.ATTR_AUTH_USER);
        if (value instanceof AuthenticatedPayrollUser user) {
            requirePayrollProductScope(user);
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

    private void ensureTenantAccess(final String tenantCode, final AuthenticatedPayrollUser actor) {
        if (tenantCode.equalsIgnoreCase(actor.tenantCode()) || hasRole(actor, "PLATFORM_ADMIN")) {
            return;
        }
        throw new PayrollForbiddenException("Tenant mismatch for payroll action");
    }

    private void requirePayrollProductScope(final AuthenticatedPayrollUser actor) {
        if (actor.products().contains("PAYROLL")
            || actor.products().contains("HRMS")) {
            return;
        }
        throw new PayrollForbiddenException("User does not have payroll product access");
    }
}
