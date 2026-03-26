package com.nexra.hrms.nexra.modules.payroll.controller;

import com.nexra.hrms.nexra.modules.payroll.dto.ApiResponse;
import com.nexra.hrms.nexra.modules.payroll.dto.EmployeeProfileUpsertRequest;
import com.nexra.hrms.nexra.modules.payroll.dto.OrganizationProfileUpsertRequest;
import com.nexra.hrms.nexra.modules.payroll.exception.PayrollForbiddenException;
import com.nexra.hrms.nexra.modules.payroll.exception.PayrollUnauthorizedException;
import com.nexra.hrms.nexra.modules.payroll.model.EmployeeProfile;
import com.nexra.hrms.nexra.modules.payroll.model.OrganizationProfile;
import com.nexra.hrms.nexra.modules.payroll.security.AuthenticatedPayrollUser;
import com.nexra.hrms.nexra.modules.payroll.security.PayrollAuthFilter;
import com.nexra.hrms.nexra.modules.payroll.service.ProfileDirectoryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
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

/**
 * Exposes payroll profile APIs for org-level payroll defaults and employee payroll profiles.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payroll")
@Slf4j
@Validated
public class PayrollProfileController {

    private final ProfileDirectoryService profileDirectoryService;

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

    @PostMapping("/employees")
    public ResponseEntity<ApiResponse<EmployeeProfile>> createEmployeeProfile(
        @Valid @RequestBody final EmployeeProfileUpsertRequest request,
        final HttpServletRequest httpRequest
    ) {
        return upsertEmployeeProfile(request, httpRequest);
    }

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
