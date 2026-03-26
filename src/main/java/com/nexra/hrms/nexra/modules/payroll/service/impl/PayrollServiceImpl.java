package com.nexra.hrms.nexra.modules.payroll.service.impl;

import com.nexra.hrms.nexra.modules.payroll.dto.ProfilePayrollGenerationRequest;
import com.nexra.hrms.nexra.modules.payroll.dto.PayrollGenerationRequest;
import com.nexra.hrms.nexra.modules.payroll.dto.PayrollLineItemRequest;
import com.nexra.hrms.nexra.modules.payroll.exception.PayrollBusinessException;
import com.nexra.hrms.nexra.modules.payroll.exception.PayrollResourceNotFoundException;
import com.nexra.hrms.nexra.modules.payroll.model.AuthDependencyStatus;
import com.nexra.hrms.nexra.modules.payroll.model.EmployeeProfile;
import com.nexra.hrms.nexra.modules.payroll.model.OrganizationProfile;
import com.nexra.hrms.nexra.modules.payroll.model.PayrollLineItem;
import com.nexra.hrms.nexra.modules.payroll.model.PayrollSlip;
import com.nexra.hrms.nexra.modules.payroll.security.AuthenticatedPayrollUser;
import com.nexra.hrms.nexra.modules.payroll.service.AuthReferenceClient;
import com.nexra.hrms.nexra.modules.payroll.service.PayrollService;
import com.nexra.hrms.nexra.modules.payroll.service.ProfileDirectoryService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Implements payroll generation and payslip retrieval workflows using in-memory slip storage.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@Service
@Slf4j
public class PayrollServiceImpl implements PayrollService {

    private final Map<String, PayrollSlip> slips = new ConcurrentHashMap<>();
    private final AuthReferenceClient authReferenceClient;
    private final ProfileDirectoryService profileDirectoryService;

    public PayrollServiceImpl(
        final AuthReferenceClient authReferenceClient,
        final ProfileDirectoryService profileDirectoryService
    ) {
        this.authReferenceClient = authReferenceClient;
        this.profileDirectoryService = profileDirectoryService;
    }

    @Override
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
        slips.put(slip.slipId(), slip);
        log.info("PayrollServiceImpl - generatePayrollInternal - slipGenerated slipId={}, tenantCode={}, employeeId={}",
            slip.slipId(), slip.tenantCode(), slip.employeeId());
        return slip;
    }

    @Override
    public PayrollSlip getSlip(final String slipId) {
        PayrollSlip slip = slips.get(slipId);
        if (slip == null) {
            throw new PayrollResourceNotFoundException("Payroll slip not found: " + slipId);
        }
        return slip;
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
        return slips.values().stream()
            .filter(slip -> slip.tenantCode().equalsIgnoreCase(tenantCode))
            .sorted((left, right) -> right.generatedAt().compareTo(left.generatedAt()))
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
}
