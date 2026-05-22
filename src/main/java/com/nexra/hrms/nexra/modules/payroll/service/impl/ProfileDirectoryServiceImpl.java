package com.nexra.hrms.nexra.modules.payroll.service.impl;

import com.nexra.hrms.nexra.modules.payroll.dto.EmployeeProfileUpsertRequest;
import com.nexra.hrms.nexra.modules.payroll.dto.OrganizationProfileUpsertRequest;
import com.nexra.hrms.nexra.modules.payroll.entity.PayrollEmployeeProfileEntity;
import com.nexra.hrms.nexra.modules.payroll.entity.PayrollOrganizationProfileEntity;
import com.nexra.hrms.nexra.modules.payroll.exception.PayrollBusinessException;
import com.nexra.hrms.nexra.modules.payroll.exception.PayrollResourceNotFoundException;
import com.nexra.hrms.nexra.modules.payroll.model.EmployeeProfile;
import com.nexra.hrms.nexra.modules.payroll.model.OrganizationProfile;
import com.nexra.hrms.nexra.modules.payroll.repository.PayrollEmployeeProfileRepository;
import com.nexra.hrms.nexra.modules.payroll.repository.PayrollOrganizationProfileRepository;
import com.nexra.hrms.nexra.modules.payroll.security.AuthenticatedPayrollUser;
import com.nexra.hrms.nexra.modules.payroll.service.ProfileDirectoryService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistent payroll profile directory for organization and employee reference profiles.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ProfileDirectoryServiceImpl implements ProfileDirectoryService {

    private final PayrollOrganizationProfileRepository organizationProfileRepository;
    private final PayrollEmployeeProfileRepository employeeProfileRepository;

    @Override
    @Transactional
    public OrganizationProfile upsertOrganizationProfile(
        final OrganizationProfileUpsertRequest request,
        final AuthenticatedPayrollUser actor
    ) {
        verifyTenant(actor, request.tenantCode());
        String tenantCode = request.tenantCode().trim().toUpperCase();
        PayrollOrganizationProfileEntity entity = organizationProfileRepository.findByTenantCodeIgnoreCase(tenantCode)
            .orElseGet(PayrollOrganizationProfileEntity::new);
        if (entity.getId() == null) {
            entity.setId(UUID.randomUUID().toString());
            entity.setCreatedBy(actor.email());
        }

        entity.setTenantCode(tenantCode);
        entity.setOrganizationName(request.organizationName().trim());
        entity.setLegalEntityName(request.legalEntityName().trim());
        entity.setAddressLine1(request.addressLine1().trim());
        entity.setAddressLine2(blankToNull(request.addressLine2()));
        entity.setCity(request.city().trim());
        entity.setState(request.state().trim());
        entity.setCountry(request.country().trim());
        entity.setPostalCode(request.postalCode().trim());
        entity.setCurrency(request.currency().trim().toUpperCase());
        entity.setDefaultTaxPercent(amountOrZero(request.defaultTaxPercent()));
        entity.setDefaultProvidentFundPercent(amountOrZero(request.defaultProvidentFundPercent()));
        entity.setPayrollContactEmail(blankToNullLower(request.payrollContactEmail()));
        entity.setPayrollContactPhone(blankToNull(request.payrollContactPhone()));
        entity.setBrandingLogoPath(blankToNull(request.brandingLogoPath()));
        entity.setBrandingCompanyName(blankToNull(request.brandingCompanyName()));
        entity.setBrandingWatermarkText(blankToNull(request.brandingWatermarkText()));
        entity.setUpdatedBy(actor.email());

        PayrollOrganizationProfileEntity saved = organizationProfileRepository.save(entity);
        log.info("ProfileDirectoryServiceImpl - upsertOrganizationProfile - tenantCode={}, actor={}",
            saved.getTenantCode(), actor.email());
        return toModel(saved);
    }

    @Override
    public OrganizationProfile getOrganizationProfile(final String tenantCode, final AuthenticatedPayrollUser actor) {
        verifyTenant(actor, tenantCode);
        return getOrganizationProfileInternal(tenantCode);
    }

    @Override
    @Transactional
    public EmployeeProfile upsertEmployeeProfile(
        final EmployeeProfileUpsertRequest request,
        final AuthenticatedPayrollUser actor
    ) {
        verifyTenant(actor, request.tenantCode());
        String tenantCode = request.tenantCode().trim().toUpperCase();
        PayrollEmployeeProfileEntity entity = employeeProfileRepository.findByTenantCodeIgnoreCaseAndEmployeeId(tenantCode, request.employeeId())
            .orElseGet(PayrollEmployeeProfileEntity::new);
        if (entity.getId() == null) {
            entity.setId(UUID.randomUUID().toString());
            entity.setCreatedBy(actor.email());
        }

        entity.setTenantCode(tenantCode);
        entity.setEmployeeId(request.employeeId().trim());
        entity.setEmployeeCode(request.employeeCode().trim());
        entity.setEmployeeName(request.employeeName().trim());
        entity.setDepartment(request.department().trim());
        entity.setDesignation(request.designation().trim());
        entity.setMonthlyBasicSalary(request.monthlyBasicSalary().setScale(2, RoundingMode.HALF_UP));
        entity.setBankName(blankToNull(request.bankName()));
        entity.setBankAccountMasked(blankToNull(request.bankAccountMasked()));
        entity.setPanMasked(blankToNull(request.panMasked()));
        entity.setUanMasked(blankToNull(request.uanMasked()));
        entity.setEmail(blankToNullLower(request.email()));
        entity.setUpdatedBy(actor.email());

        PayrollEmployeeProfileEntity saved = employeeProfileRepository.save(entity);
        log.info("ProfileDirectoryServiceImpl - upsertEmployeeProfile - tenantCode={}, employeeId={}, actor={}",
            saved.getTenantCode(), saved.getEmployeeId(), actor.email());
        return toModel(saved);
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
        return employeeProfileRepository.findByTenantCodeIgnoreCaseOrderByEmployeeCodeAsc(tenantCode.trim().toUpperCase()).stream()
            .map(this::toModel)
            .toList();
    }

    @Override
    @Transactional
    public OrganizationProfile updateOrganizationBrandingLogoPath(
        final String tenantCode,
        final String logoPath,
        final AuthenticatedPayrollUser actor
    ) {
        verifyTenant(actor, tenantCode);
        PayrollOrganizationProfileEntity entity = organizationProfileRepository
            .findByTenantCodeIgnoreCase(tenantCode.trim().toUpperCase())
            .orElseThrow(() -> new PayrollResourceNotFoundException("Organization profile not found for tenant: " + tenantCode));
        entity.setBrandingLogoPath(blankToNull(logoPath));
        entity.setUpdatedBy(actor.email());
        return toModel(organizationProfileRepository.save(entity));
    }

    @Override
    public OrganizationProfile getOrganizationProfileInternal(final String tenantCode) {
        return organizationProfileRepository.findByTenantCodeIgnoreCase(tenantCode.trim().toUpperCase())
            .map(this::toModel)
            .orElseThrow(() -> new PayrollResourceNotFoundException("Organization profile not found for tenant: " + tenantCode));
    }

    @Override
    public EmployeeProfile getEmployeeProfileInternal(final String tenantCode, final String employeeId) {
        return employeeProfileRepository.findByTenantCodeIgnoreCaseAndEmployeeId(tenantCode.trim().toUpperCase(), employeeId.trim())
            .map(this::toModel)
            .orElseThrow(() -> new PayrollResourceNotFoundException("Employee profile not found: " + employeeId + " for tenant " + tenantCode));
    }

    private void verifyTenant(final AuthenticatedPayrollUser actor, final String tenantCode) {
        if (!actor.tenantCode().equalsIgnoreCase(tenantCode)) {
            throw new PayrollBusinessException("Token tenant does not match requested tenant");
        }
    }

    private String blankToNull(final String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String blankToNullLower(final String value) {
        String trimmed = blankToNull(value);
        return trimmed == null ? null : trimmed.toLowerCase();
    }

    private BigDecimal amountOrZero(final BigDecimal value) {
        return (value == null ? BigDecimal.ZERO : value).setScale(2, RoundingMode.HALF_UP);
    }

    private OrganizationProfile toModel(final PayrollOrganizationProfileEntity entity) {
        return new OrganizationProfile(
            entity.getTenantCode(),
            entity.getOrganizationName(),
            entity.getLegalEntityName(),
            entity.getAddressLine1(),
            entity.getAddressLine2(),
            entity.getCity(),
            entity.getState(),
            entity.getCountry(),
            entity.getPostalCode(),
            entity.getCurrency(),
            entity.getDefaultTaxPercent(),
            entity.getDefaultProvidentFundPercent(),
            entity.getPayrollContactEmail(),
            entity.getPayrollContactPhone(),
            entity.getBrandingLogoPath(),
            entity.getBrandingCompanyName(),
            entity.getBrandingWatermarkText(),
            entity.getUpdatedAt(),
            entity.getUpdatedBy()
        );
    }

    private EmployeeProfile toModel(final PayrollEmployeeProfileEntity entity) {
        return new EmployeeProfile(
            entity.getTenantCode(),
            entity.getEmployeeId(),
            entity.getEmployeeCode(),
            entity.getEmployeeName(),
            entity.getDepartment(),
            entity.getDesignation(),
            entity.getMonthlyBasicSalary(),
            entity.getBankName(),
            entity.getBankAccountMasked(),
            entity.getPanMasked(),
            entity.getUanMasked(),
            entity.getEmail(),
            entity.getUpdatedAt(),
            entity.getUpdatedBy()
        );
    }
}
