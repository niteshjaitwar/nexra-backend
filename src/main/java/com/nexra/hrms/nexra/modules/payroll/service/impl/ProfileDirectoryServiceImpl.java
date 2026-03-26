package com.nexra.hrms.nexra.modules.payroll.service.impl;

import com.nexra.hrms.nexra.modules.payroll.dto.EmployeeProfileUpsertRequest;
import com.nexra.hrms.nexra.modules.payroll.dto.OrganizationProfileUpsertRequest;
import com.nexra.hrms.nexra.modules.payroll.exception.PayrollBusinessException;
import com.nexra.hrms.nexra.modules.payroll.exception.PayrollResourceNotFoundException;
import com.nexra.hrms.nexra.modules.payroll.model.EmployeeProfile;
import com.nexra.hrms.nexra.modules.payroll.model.OrganizationProfile;
import com.nexra.hrms.nexra.modules.payroll.security.AuthenticatedPayrollUser;
import com.nexra.hrms.nexra.modules.payroll.service.ProfileDirectoryService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * In-memory payroll profile directory for organization and employee reference profiles.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@Service
@Slf4j
public class ProfileDirectoryServiceImpl implements ProfileDirectoryService {

    private final Map<String, OrganizationProfile> organizationProfiles = new ConcurrentHashMap<>();
    private final Map<String, Map<String, EmployeeProfile>> employeeProfiles = new ConcurrentHashMap<>();

    @Override
    public OrganizationProfile upsertOrganizationProfile(
        final OrganizationProfileUpsertRequest request,
        final AuthenticatedPayrollUser actor
    ) {
        verifyTenant(actor, request.tenantCode());
        OrganizationProfile profile = new OrganizationProfile(
            request.tenantCode(),
            request.organizationName(),
            request.legalEntityName(),
            request.addressLine1(),
            blankToNull(request.addressLine2()),
            request.city(),
            request.state(),
            request.country(),
            request.postalCode(),
            request.currency().trim().toUpperCase(),
            amountOrZero(request.defaultTaxPercent()),
            amountOrZero(request.defaultProvidentFundPercent()),
            blankToNull(request.payrollContactEmail()),
            blankToNull(request.payrollContactPhone()),
            Instant.now(),
            actor.email()
        );
        organizationProfiles.put(profile.tenantCode().toUpperCase(), profile);
        log.info("ProfileDirectoryServiceImpl - upsertOrganizationProfile - tenantCode={}, actor={}",
            profile.tenantCode(), actor.email());
        return profile;
    }

    @Override
    public OrganizationProfile getOrganizationProfile(final String tenantCode, final AuthenticatedPayrollUser actor) {
        verifyTenant(actor, tenantCode);
        OrganizationProfile profile = organizationProfiles.get(tenantCode.toUpperCase());
        if (profile == null) {
            throw new PayrollResourceNotFoundException("Organization profile not found for tenant: " + tenantCode);
        }
        return profile;
    }

    @Override
    public EmployeeProfile upsertEmployeeProfile(
        final EmployeeProfileUpsertRequest request,
        final AuthenticatedPayrollUser actor
    ) {
        verifyTenant(actor, request.tenantCode());
        EmployeeProfile profile = new EmployeeProfile(
            request.tenantCode(),
            request.employeeId(),
            request.employeeCode(),
            request.employeeName(),
            request.department(),
            request.designation(),
            request.monthlyBasicSalary().setScale(2, RoundingMode.HALF_UP),
            blankToNull(request.bankName()),
            blankToNull(request.bankAccountMasked()),
            blankToNull(request.panMasked()),
            blankToNull(request.uanMasked()),
            blankToNull(request.email()),
            Instant.now(),
            actor.email()
        );

        employeeProfiles.computeIfAbsent(profile.tenantCode().toUpperCase(), ignored -> new ConcurrentHashMap<>())
            .put(profile.employeeId(), profile);
        log.info("ProfileDirectoryServiceImpl - upsertEmployeeProfile - tenantCode={}, employeeId={}, actor={}",
            profile.tenantCode(), profile.employeeId(), actor.email());
        return profile;
    }

    @Override
    public EmployeeProfile getEmployeeProfile(
        final String tenantCode,
        final String employeeId,
        final AuthenticatedPayrollUser actor
    ) {
        verifyTenant(actor, tenantCode);
        return getEmployeeProfileInternal(tenantCode, employeeId);
    }

    @Override
    public List<EmployeeProfile> listEmployeeProfiles(final String tenantCode, final AuthenticatedPayrollUser actor) {
        verifyTenant(actor, tenantCode);
        return employeeProfiles.getOrDefault(tenantCode.toUpperCase(), Map.of()).values().stream()
            .sorted(Comparator.comparing(EmployeeProfile::employeeCode))
            .toList();
    }

    @Override
    public OrganizationProfile getOrganizationProfileInternal(final String tenantCode) {
        OrganizationProfile profile = organizationProfiles.get(tenantCode.toUpperCase());
        if (profile == null) {
            throw new PayrollResourceNotFoundException("Organization profile not found for tenant: " + tenantCode);
        }
        return profile;
    }

    @Override
    public EmployeeProfile getEmployeeProfileInternal(final String tenantCode, final String employeeId) {
        EmployeeProfile profile = employeeProfiles.getOrDefault(tenantCode.toUpperCase(), Map.of()).get(employeeId);
        if (profile == null) {
            throw new PayrollResourceNotFoundException("Employee profile not found: " + employeeId + " for tenant " + tenantCode);
        }
        return profile;
    }

    private void verifyTenant(final AuthenticatedPayrollUser actor, final String tenantCode) {
        if (!actor.tenantCode().equalsIgnoreCase(tenantCode)) {
            throw new PayrollBusinessException("Token tenant does not match requested tenant");
        }
    }

    private String blankToNull(final String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private BigDecimal amountOrZero(final BigDecimal value) {
        return (value == null ? BigDecimal.ZERO : value).setScale(2, RoundingMode.HALF_UP);
    }
}
