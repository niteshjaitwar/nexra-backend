package com.nexra.hrms.nexra.modules.payroll.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexra.hrms.nexra.modules.payroll.dto.ProfilePayrollGenerationRequest;
import com.nexra.hrms.nexra.modules.payroll.dto.PayrollGenerationRequest;
import com.nexra.hrms.nexra.modules.payroll.dto.PayrollLineItemRequest;
import com.nexra.hrms.nexra.modules.payroll.entity.PayrollSlipEntity;
import com.nexra.hrms.nexra.modules.payroll.exception.PayrollBusinessException;
import com.nexra.hrms.nexra.modules.payroll.exception.PayrollResourceNotFoundException;
import com.nexra.hrms.nexra.modules.payroll.model.AuthDependencyStatus;
import com.nexra.hrms.nexra.modules.payroll.model.EmployeeProfile;
import com.nexra.hrms.nexra.modules.payroll.model.OrganizationProfile;
import com.nexra.hrms.nexra.modules.payroll.model.PayrollLineItem;
import com.nexra.hrms.nexra.modules.payroll.model.PayrollSlip;
import com.nexra.hrms.nexra.modules.payroll.repository.PayrollSlipRepository;
import com.nexra.hrms.nexra.modules.payroll.security.AuthenticatedPayrollUser;
import com.nexra.hrms.nexra.modules.payroll.service.AuthReferenceClient;
import com.nexra.hrms.nexra.modules.payroll.service.PayrollService;
import com.nexra.hrms.nexra.modules.payroll.service.ProfileDirectoryService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implements payroll generation and payslip retrieval workflows backed by persistent payroll slip storage.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@Service
@Slf4j
public class PayrollServiceImpl implements PayrollService {

    private static final TypeReference<List<PayrollLineItem>> PAYROLL_LINE_ITEMS = new TypeReference<>() { };
    private final ObjectMapper objectMapper;
    private final AuthReferenceClient authReferenceClient;
    private final ProfileDirectoryService profileDirectoryService;
    private final PayrollSlipRepository payrollSlipRepository;

    public PayrollServiceImpl(
        final ObjectMapper objectMapper,
        final AuthReferenceClient authReferenceClient,
        final ProfileDirectoryService profileDirectoryService,
        final PayrollSlipRepository payrollSlipRepository
    ) {
        this.objectMapper = objectMapper;
        this.authReferenceClient = authReferenceClient;
        this.profileDirectoryService = profileDirectoryService;
        this.payrollSlipRepository = payrollSlipRepository;
    }

    @Override
    @Transactional
    public PayrollSlip generatePayroll(final PayrollGenerationRequest request, final AuthenticatedPayrollUser actor) {
        if (!actor.tenantCode().equalsIgnoreCase(request.tenantCode())) {
            throw new PayrollBusinessException("Token tenant does not match request tenant");
        }
        log.info("PayrollServiceImpl - generatePayroll - tenantCode={}, employeeId={}, payPeriod={}",
            request.tenantCode(), request.employeeId(), request.payPeriod());
        OrganizationProfile organizationProfile = safeOrganizationProfile(request.tenantCode());
        EmployeeProfile employeeProfile = null;
        try {
            employeeProfile = profileDirectoryService.getEmployeeProfileInternal(request.tenantCode(), request.employeeId());
        } catch (PayrollResourceNotFoundException ignored) {
            // Full-payload payroll generation remains supported even if employee profile isn't registered yet.
        }
        return generatePayrollInternal(
            request.tenantCode(),
            request.employeeId(),
            request.employeeCode(),
            request.employeeName(),
            request.department(),
            request.designation(),
            request.payPeriod(),
            request.currency(),
            request.basicSalary(),
            request.allowances(),
            request.deductions(),
            request.taxPercent(),
            request.providentFundPercent(),
            organizationProfile,
            employeeProfile,
            actor
        );
    }

    @Override
    @Transactional
    public PayrollSlip generatePayrollFromProfile(
        final ProfilePayrollGenerationRequest request,
        final AuthenticatedPayrollUser actor
    ) {
        if (!actor.tenantCode().equalsIgnoreCase(request.tenantCode())) {
            throw new PayrollBusinessException("Token tenant does not match request tenant");
        }
        log.info("PayrollServiceImpl - generatePayrollFromProfile - tenantCode={}, employeeId={}, payPeriod={}",
            request.tenantCode(), request.employeeId(), request.payPeriod());
        OrganizationProfile org = profileDirectoryService.getOrganizationProfileInternal(request.tenantCode());
        EmployeeProfile employee = profileDirectoryService.getEmployeeProfileInternal(request.tenantCode(), request.employeeId());
        return generatePayrollInternal(
            request.tenantCode(),
            employee.employeeId(),
            employee.employeeCode(),
            employee.employeeName(),
            employee.department(),
            employee.designation(),
            request.payPeriod(),
            request.currencyOverride() == null || request.currencyOverride().isBlank() ? org.currency() : request.currencyOverride(),
            employee.monthlyBasicSalary(),
            request.allowances(),
            request.deductions(),
            request.taxPercentOverride() == null ? org.defaultTaxPercent() : request.taxPercentOverride(),
            request.providentFundPercentOverride() == null ? org.defaultProvidentFundPercent() : request.providentFundPercentOverride(),
            org,
            employee,
            actor
        );
    }

    private PayrollSlip generatePayrollInternal(
        final String tenantCode,
        final String employeeId,
        final String employeeCode,
        final String employeeName,
        final String department,
        final String designation,
        final String payPeriod,
        final String currency,
        final BigDecimal basicSalary,
        final List<PayrollLineItemRequest> allowanceRequests,
        final List<PayrollLineItemRequest> deductionRequests,
        final BigDecimal taxPercentInput,
        final BigDecimal pfPercentInput,
        final OrganizationProfile organizationProfile,
        final EmployeeProfile employeeProfile,
        final AuthenticatedPayrollUser actor
    ) {
        List<PayrollLineItem> allowances = normalizeItems(allowanceRequests);
        List<PayrollLineItem> manualDeductions = normalizeItems(deductionRequests);
        BigDecimal basic = amount(basicSalary);
        BigDecimal gross = basic.add(sum(allowances));
        BigDecimal taxPercent = amountOrZero(taxPercentInput);
        BigDecimal pfPercent = amountOrZero(pfPercentInput);
        BigDecimal taxAmount = percent(gross, taxPercent);
        BigDecimal pfAmount = percent(gross, pfPercent);
        BigDecimal totalDeductions = sum(manualDeductions).add(taxAmount).add(pfAmount).setScale(2, RoundingMode.HALF_UP);
        BigDecimal netPay = gross.subtract(totalDeductions).setScale(2, RoundingMode.HALF_UP);

        AuthDependencyStatus authHealth = authReferenceClient.getAuthHealth();

        PayrollSlip slip = new PayrollSlip(
            UUID.randomUUID().toString(),
            tenantCode,
            employeeId,
            employeeCode,
            employeeName,
            department,
            designation,
            payPeriod,
            currency == null || currency.isBlank() ? "INR" : currency.trim().toUpperCase(),
            organizationProfile,
            employeeProfile,
            basic,
            allowances,
            manualDeductions,
            taxPercent,
            pfPercent,
            taxAmount,
            pfAmount,
            gross.setScale(2, RoundingMode.HALF_UP),
            totalDeductions,
            netPay,
            Instant.now(),
            actor.email(),
            actor.userId().toString(),
            authHealth
        );
        payrollSlipRepository.save(toEntity(slip));
        log.info("PayrollServiceImpl - generatePayrollInternal - slipGenerated slipId={}, tenantCode={}, employeeId={}",
            slip.slipId(), slip.tenantCode(), slip.employeeId());
        return slip;
    }

    @Override
    public PayrollSlip getSlip(final String slipId) {
        return payrollSlipRepository.findById(slipId)
            .map(this::toModel)
            .orElseThrow(() -> new PayrollResourceNotFoundException("Payroll slip not found: " + slipId));
    }

    @Override
    public AuthDependencyStatus getAuthDependencyStatus() {
        return authReferenceClient.getAuthHealth();
    }

    @Override
    public List<PayrollSlip> listSlipsForTenant(final String tenantCode, final AuthenticatedPayrollUser actor) {
        if (!actor.tenantCode().equalsIgnoreCase(tenantCode)) {
            throw new PayrollBusinessException("Token tenant does not match requested tenant");
        }
        return payrollSlipRepository.findByTenantCodeIgnoreCaseOrderByGeneratedAtDesc(tenantCode.trim().toUpperCase()).stream()
            .map(this::toModel)
            .toList();
    }

    private List<PayrollLineItem> normalizeItems(final List<PayrollLineItemRequest> items) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }
        return items.stream()
            .map(item -> new PayrollLineItem(item.name(), amount(item.amount())))
            .toList();
    }

    private BigDecimal sum(final List<PayrollLineItem> items) {
        return items.stream()
            .map(PayrollLineItem::amount)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal percent(final BigDecimal amount, final BigDecimal percent) {
        return amount
            .multiply(percent)
            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal amount(final BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal amountOrZero(final BigDecimal value) {
        return value == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : amount(value);
    }

    private OrganizationProfile safeOrganizationProfile(final String tenantCode) {
        try {
            return profileDirectoryService.getOrganizationProfileInternal(tenantCode);
        } catch (PayrollResourceNotFoundException ex) {
            log.debug("PayrollServiceImpl - safeOrganizationProfile - profile unavailable for tenantCode={}", tenantCode);
            return null;
        }
    }

    private PayrollSlipEntity toEntity(final PayrollSlip slip) {
        PayrollSlipEntity entity = new PayrollSlipEntity();
        entity.setSlipId(slip.slipId());
        entity.setTenantCode(slip.tenantCode().trim().toUpperCase());
        entity.setEmployeeId(slip.employeeId());
        entity.setEmployeeCode(slip.employeeCode());
        entity.setEmployeeName(slip.employeeName());
        entity.setDepartment(slip.department());
        entity.setDesignation(slip.designation());
        entity.setPayPeriod(slip.payPeriod());
        entity.setCurrency(slip.currency());
        entity.setOrganizationProfileJson(writeJson(slip.organizationProfile()));
        entity.setEmployeeProfileJson(slip.employeeProfile() == null ? null : writeJson(slip.employeeProfile()));
        entity.setAllowancesJson(writeJson(slip.allowances()));
        entity.setDeductionsJson(writeJson(slip.deductions()));
        entity.setAuthDependencyStatusJson(writeJson(slip.authDependencyStatus()));
        entity.setBasicSalary(slip.basicSalary());
        entity.setTaxPercent(slip.taxPercent());
        entity.setProvidentFundPercent(slip.providentFundPercent());
        entity.setTaxAmount(slip.taxAmount());
        entity.setProvidentFundAmount(slip.providentFundAmount());
        entity.setGrossEarnings(slip.grossEarnings());
        entity.setTotalDeductions(slip.totalDeductions());
        entity.setNetPay(slip.netPay());
        entity.setGeneratedAt(slip.generatedAt());
        entity.setGeneratedByEmail(slip.generatedByEmail());
        entity.setGeneratedByUserId(slip.generatedByUserId());
        return entity;
    }

    private PayrollSlip toModel(final PayrollSlipEntity entity) {
        return new PayrollSlip(
            entity.getSlipId(),
            entity.getTenantCode(),
            entity.getEmployeeId(),
            entity.getEmployeeCode(),
            entity.getEmployeeName(),
            entity.getDepartment(),
            entity.getDesignation(),
            entity.getPayPeriod(),
            entity.getCurrency(),
            readJson(entity.getOrganizationProfileJson(), OrganizationProfile.class),
            entity.getEmployeeProfileJson() == null ? null : readJson(entity.getEmployeeProfileJson(), EmployeeProfile.class),
            entity.getBasicSalary(),
            readJson(entity.getAllowancesJson(), PAYROLL_LINE_ITEMS),
            readJson(entity.getDeductionsJson(), PAYROLL_LINE_ITEMS),
            entity.getTaxPercent(),
            entity.getProvidentFundPercent(),
            entity.getTaxAmount(),
            entity.getProvidentFundAmount(),
            entity.getGrossEarnings(),
            entity.getTotalDeductions(),
            entity.getNetPay(),
            entity.getGeneratedAt(),
            entity.getGeneratedByEmail(),
            entity.getGeneratedByUserId(),
            readJson(entity.getAuthDependencyStatusJson(), AuthDependencyStatus.class)
        );
    }

    private String writeJson(final Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to serialize payroll snapshot", exception);
        }
    }

    private <T> T readJson(final String value, final Class<T> type) {
        try {
            return objectMapper.readValue(value, type);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to deserialize payroll snapshot", exception);
        }
    }

    private <T> T readJson(final String value, final TypeReference<T> type) {
        try {
            return objectMapper.readValue(value, type);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to deserialize payroll snapshot", exception);
        }
    }
}
