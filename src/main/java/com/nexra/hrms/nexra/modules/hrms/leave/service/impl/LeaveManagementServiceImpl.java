package com.nexra.hrms.nexra.modules.hrms.leave.service.impl;

import com.nexra.hrms.nexra.modules.hrms.leave.dto.request.HolidayUpsertRequest;
import com.nexra.hrms.nexra.modules.hrms.leave.dto.request.LeaveBalanceAdjustRequest;
import com.nexra.hrms.nexra.modules.hrms.leave.dto.request.LeaveDecisionRequest;
import com.nexra.hrms.nexra.modules.hrms.leave.dto.request.LeaveRequestCreateRequest;
import com.nexra.hrms.nexra.modules.hrms.leave.dto.request.LeaveTypeUpsertRequest;
import com.nexra.hrms.nexra.modules.hrms.leave.entity.HolidayEntity;
import com.nexra.hrms.nexra.modules.hrms.leave.entity.LeaveBalanceEntity;
import com.nexra.hrms.nexra.modules.hrms.leave.entity.LeaveRequestEntity;
import com.nexra.hrms.nexra.modules.hrms.leave.entity.LeaveTypeEntity;
import com.nexra.hrms.nexra.modules.hrms.leave.exception.LeaveBusinessException;
import com.nexra.hrms.nexra.modules.hrms.leave.exception.LeaveForbiddenException;
import com.nexra.hrms.nexra.modules.hrms.leave.exception.LeaveResourceNotFoundException;
import com.nexra.hrms.nexra.modules.hrms.leave.model.Holiday;
import com.nexra.hrms.nexra.modules.hrms.leave.model.LeaveBalance;
import com.nexra.hrms.nexra.modules.hrms.leave.model.LeaveRequestView;
import com.nexra.hrms.nexra.modules.hrms.leave.model.LeaveType;
import com.nexra.hrms.nexra.modules.hrms.leave.repository.HolidayRepository;
import com.nexra.hrms.nexra.modules.hrms.leave.repository.LeaveBalanceRepository;
import com.nexra.hrms.nexra.modules.hrms.leave.repository.LeaveRequestRepository;
import com.nexra.hrms.nexra.modules.hrms.leave.repository.LeaveTypeRepository;
import com.nexra.hrms.nexra.modules.hrms.leave.security.AuthenticatedLeaveUser;
import com.nexra.hrms.nexra.modules.hrms.leave.service.LeaveManagementService;
import com.nexra.hrms.nexra.modules.hrms.employee.repository.EmployeeRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implements leave and holiday management workflows with tenant isolation and approval rules.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LeaveManagementServiceImpl implements LeaveManagementService {

    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_APPROVED = "APPROVED";
    private static final String STATUS_REJECTED = "REJECTED";

    private final LeaveTypeRepository leaveTypeRepository;
    private final HolidayRepository holidayRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    @Transactional
    public LeaveType upsertLeaveType(final LeaveTypeUpsertRequest request, final AuthenticatedLeaveUser actor) {
        verifyTenant(actor, request.tenantCode());
        String tenant = normTenant(request.tenantCode());
        String code = trim(request.code()).toUpperCase();
        log.info("LeaveManagementServiceImpl - upsertLeaveType - tenantCode={}, code={}, actor={}", tenant, code, actor.email());

        LeaveTypeEntity entity = leaveTypeRepository.findByTenantCodeIgnoreCaseAndCodeIgnoreCase(tenant, code)
            .orElseGet(LeaveTypeEntity::new);
        if (entity.getId() == null) {
            entity.setId(UUID.randomUUID().toString());
            entity.setCreatedBy(actor.email());
        }
        entity.setTenantCode(tenant);
        entity.setCode(code);
        entity.setName(trim(request.name()));
        entity.setPaid(request.paid());
        entity.setDefaultAnnualQuota(amount(request.defaultAnnualQuota()));
        entity.setActive(request.active() == null || request.active());
        entity.setUpdatedBy(actor.email());
        return toLeaveType(leaveTypeRepository.save(entity));
    }

    @Override
    public List<LeaveType> listLeaveTypes(final String tenantCode, final boolean includeInactive, final AuthenticatedLeaveUser actor) {
        verifyTenant(actor, tenantCode);
        return leaveTypeRepository.findByTenantCodeIgnoreCaseOrderByCodeAsc(normTenant(tenantCode)).stream()
            .filter(item -> includeInactive || item.isActive())
            .map(this::toLeaveType)
            .toList();
    }

    @Override
    @Transactional
    public Holiday upsertHoliday(final HolidayUpsertRequest request, final AuthenticatedLeaveUser actor) {
        verifyTenant(actor, request.tenantCode());
        String tenant = normTenant(request.tenantCode());
        String location = blankToNullUpper(request.locationCode());
        log.info("LeaveManagementServiceImpl - upsertHoliday - tenantCode={}, holidayDate={}, actor={}",
            tenant, request.holidayDate(), actor.email());

        HolidayEntity entity;
        if (request.holidayId() == null || request.holidayId().isBlank()) {
            entity = holidayRepository.findByTenantCodeIgnoreCaseAndHolidayDateAndLocationCode(tenant, request.holidayDate(), location)
                .orElseGet(HolidayEntity::new);
            if (entity.getId() == null) {
                entity.setId(UUID.randomUUID().toString());
                entity.setCreatedBy(actor.email());
            }
        } else {
            entity = holidayRepository.findByIdAndTenantCodeIgnoreCase(request.holidayId(), tenant)
                .orElseThrow(() -> new LeaveResourceNotFoundException("Holiday not found: " + request.holidayId()));
        }

        entity.setTenantCode(tenant);
        entity.setHolidayDate(request.holidayDate());
        entity.setName(trim(request.name()));
        entity.setLocationCode(location);
        entity.setActive(request.active() == null || request.active());
        entity.setUpdatedBy(actor.email());
        return toHoliday(holidayRepository.save(entity));
    }

    @Override
    public List<Holiday> listHolidays(
        final String tenantCode,
        final LocalDate fromDate,
        final LocalDate toDate,
        final AuthenticatedLeaveUser actor
    ) {
        verifyTenant(actor, tenantCode);
        LocalDate from = fromDate == null ? LocalDate.now().withDayOfYear(1) : fromDate;
        LocalDate to = toDate == null ? from.plusMonths(12) : toDate;
        if (to.isBefore(from)) {
            throw new LeaveBusinessException("toDate must be on or after fromDate");
        }
        return holidayRepository.findByTenantCodeIgnoreCaseAndHolidayDateBetweenOrderByHolidayDateAsc(normTenant(tenantCode), from, to)
            .stream()
            .map(this::toHoliday)
            .toList();
    }

    @Override
    @Transactional
    public LeaveBalance adjustBalance(final LeaveBalanceAdjustRequest request, final AuthenticatedLeaveUser actor) {
        verifyTenant(actor, request.tenantCode());
        String tenant = normTenant(request.tenantCode());
        String leaveTypeCode = trim(request.leaveTypeCode()).toUpperCase();
        ensureLeaveTypeExists(tenant, leaveTypeCode);

        LeaveBalanceEntity entity = leaveBalanceRepository.findByTenantCodeIgnoreCaseAndEmployeeIdAndLeaveTypeCodeIgnoreCase(
                tenant, request.employeeId(), leaveTypeCode)
            .orElseGet(LeaveBalanceEntity::new);

        if (entity.getId() == null) {
            entity.setId(UUID.randomUUID().toString());
            entity.setCreatedBy(actor.email());
            entity.setUsedBalance(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        }

        entity.setTenantCode(tenant);
        entity.setEmployeeId(trim(request.employeeId()));
        entity.setLeaveTypeCode(leaveTypeCode);
        entity.setOpeningBalance(amount(request.openingBalance()));
        entity.setAccruedBalance(amount(request.accruedBalance()));
        entity.setAdjustedBalance(amount(request.adjustedBalance()));
        if (entity.getUsedBalance() == null) {
            entity.setUsedBalance(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        }
        entity.setUpdatedBy(actor.email());
        log.info("LeaveManagementServiceImpl - adjustBalance - tenantCode={}, employeeId={}, leaveTypeCode={}",
            tenant, request.employeeId(), leaveTypeCode);
        return toLeaveBalance(leaveBalanceRepository.save(entity));
    }

    @Override
    public List<LeaveBalance> listBalances(final String tenantCode, final String employeeId, final AuthenticatedLeaveUser actor) {
        verifyTenant(actor, tenantCode);
        if (!isAdmin(actor) && !requestingOwnEmployee(actor, employeeId)) {
            throw new LeaveForbiddenException("User cannot view leave balances for another employee");
        }
        return leaveBalanceRepository.findByTenantCodeIgnoreCaseAndEmployeeIdOrderByLeaveTypeCodeAsc(normTenant(tenantCode), trim(employeeId))
            .stream()
            .map(this::toLeaveBalance)
            .toList();
    }

    @Override
    @Transactional
    public LeaveRequestView createLeaveRequest(final LeaveRequestCreateRequest request, final AuthenticatedLeaveUser actor) {
        verifyTenant(actor, request.tenantCode());
        String tenant = normTenant(request.tenantCode());
        if (!isAdmin(actor) && !requestingOwnEmployee(actor, request.employeeId())) {
            throw new LeaveForbiddenException("User cannot create leave request for another employee");
        }
        if (request.endDate().isBefore(request.startDate())) {
            throw new LeaveBusinessException("endDate must be on or after startDate");
        }

        String leaveTypeCode = trim(request.leaveTypeCode()).toUpperCase();
        LeaveTypeEntity leaveType = leaveTypeRepository.findByTenantCodeIgnoreCaseAndCodeIgnoreCase(tenant, leaveTypeCode)
            .orElseThrow(() -> new LeaveResourceNotFoundException("Leave type not found: " + request.leaveTypeCode()));
        if (!leaveType.isActive()) {
            throw new LeaveBusinessException("Leave type is inactive: " + leaveTypeCode);
        }

        BigDecimal totalDays = BigDecimal.valueOf(ChronoUnit.DAYS.between(request.startDate(), request.endDate()) + 1L)
            .setScale(2, RoundingMode.HALF_UP);

        LeaveRequestEntity entity = new LeaveRequestEntity();
        entity.setId(UUID.randomUUID().toString());
        entity.setTenantCode(tenant);
        entity.setEmployeeId(trim(request.employeeId()));
        entity.setLeaveTypeCode(leaveTypeCode);
        entity.setStartDate(request.startDate());
        entity.setEndDate(request.endDate());
        entity.setTotalDays(totalDays);
        entity.setReason(blankToNull(request.reason()));
        entity.setStatus(STATUS_PENDING);
        entity.setCreatedBy(actor.email());
        entity.setUpdatedBy(actor.email());
        log.info("LeaveManagementServiceImpl - createLeaveRequest - tenantCode={}, employeeId={}, leaveTypeCode={}, startDate={}, endDate={}",
            tenant, request.employeeId(), leaveTypeCode, request.startDate(), request.endDate());

        return toLeaveRequest(leaveRequestRepository.save(entity));
    }

    @Override
    public com.nexra.hrms.nexra.common.api.PageResponse<LeaveRequestView> listLeaveRequests(
        final String tenantCode,
        final String employeeId,
        final String status,
        final AuthenticatedLeaveUser actor,
        final org.springframework.data.domain.Pageable pageable
    ) {
        verifyTenant(actor, tenantCode);
        String tenant = normTenant(tenantCode);
        String employeeFilter = blankToNull(employeeId);
        if (!isAdmin(actor) && employeeFilter != null && !requestingOwnEmployee(actor, employeeFilter)) {
            throw new LeaveForbiddenException("User cannot view leave requests for another employee");
        }
        String statusFilter = blankToNullUpper(status);
        org.springframework.data.domain.Page<LeaveRequestEntity> page;
        if (employeeFilter == null && statusFilter == null) {
            page = leaveRequestRepository.findByTenantCodeIgnoreCase(tenant, pageable);
        } else if (employeeFilter != null && statusFilter == null) {
            page = leaveRequestRepository.findByTenantCodeIgnoreCaseAndEmployeeId(tenant, employeeFilter, pageable);
        } else if (employeeFilter == null) {
            page = leaveRequestRepository.findByTenantCodeIgnoreCaseAndStatusIgnoreCase(tenant, statusFilter, pageable);
        } else {
            page = leaveRequestRepository.findByTenantCodeIgnoreCaseAndEmployeeIdAndStatusIgnoreCase(tenant, employeeFilter, statusFilter, pageable);
        }
        return com.nexra.hrms.nexra.common.api.PageResponse.map(
            com.nexra.hrms.nexra.common.api.PageResponse.from(page),
            this::toLeaveRequest
        );
    }

    @Override
    public LeaveRequestView getLeaveRequest(final String tenantCode, final String requestId, final AuthenticatedLeaveUser actor) {
        verifyTenant(actor, tenantCode);
        LeaveRequestEntity entity = leaveRequestRepository.findByIdAndTenantCodeIgnoreCase(requestId, normTenant(tenantCode))
            .orElseThrow(() -> new LeaveResourceNotFoundException("Leave request not found: " + requestId));
        if (!isAdmin(actor) && !requestingOwnEmployee(actor, entity.getEmployeeId())) {
            throw new LeaveForbiddenException("User cannot view leave request for another employee");
        }
        return toLeaveRequest(entity);
    }

    @Override
    @Transactional
    public LeaveRequestView approveLeaveRequest(
        final String requestId,
        final LeaveDecisionRequest request,
        final AuthenticatedLeaveUser actor
    ) {
        return decideLeaveRequest(requestId, request, actor, true);
    }

    @Override
    @Transactional
    public LeaveRequestView rejectLeaveRequest(
        final String requestId,
        final LeaveDecisionRequest request,
        final AuthenticatedLeaveUser actor
    ) {
        return decideLeaveRequest(requestId, request, actor, false);
    }

    private LeaveRequestView decideLeaveRequest(
        final String requestId,
        final LeaveDecisionRequest request,
        final AuthenticatedLeaveUser actor,
        final boolean approve
    ) {
        verifyTenant(actor, request.tenantCode());
        requireAdmin(actor);
        String tenant = normTenant(request.tenantCode());
        LeaveRequestEntity entity = leaveRequestRepository.findByIdAndTenantCodeIgnoreCase(requestId, tenant)
            .orElseThrow(() -> new LeaveResourceNotFoundException("Leave request not found: " + requestId));

        if (!STATUS_PENDING.equalsIgnoreCase(entity.getStatus())) {
            throw new LeaveBusinessException("Leave request already decided: " + entity.getStatus());
        }

        if (approve) {
            LeaveBalanceEntity balance = leaveBalanceRepository.findByTenantCodeIgnoreCaseAndEmployeeIdAndLeaveTypeCodeIgnoreCase(
                    tenant, entity.getEmployeeId(), entity.getLeaveTypeCode())
                .orElseThrow(() -> new LeaveResourceNotFoundException("Leave balance not found for employee and leave type"));
            BigDecimal available = available(balance);
            if (available.compareTo(entity.getTotalDays()) < 0) {
                throw new LeaveBusinessException("Insufficient leave balance. Available=" + available + ", requested=" + entity.getTotalDays());
            }
            balance.setUsedBalance(amount(balance.getUsedBalance().add(entity.getTotalDays())));
            balance.setUpdatedBy(actor.email());
            leaveBalanceRepository.save(balance);
            entity.setStatus(STATUS_APPROVED);
        } else {
            entity.setStatus(STATUS_REJECTED);
        }

        entity.setApproverUserId(actor.userId().toString());
        entity.setApproverEmail(actor.email());
        entity.setDecisionComment(blankToNull(request.decisionComment()));
        entity.setUpdatedBy(actor.email());
        log.info("LeaveManagementServiceImpl - decideLeaveRequest - tenantCode={}, requestId={}, action={}, approver={}",
            tenant, requestId, approve ? "APPROVE" : "REJECT", actor.email());
        return toLeaveRequest(leaveRequestRepository.save(entity));
    }

    private void ensureLeaveTypeExists(final String tenant, final String leaveTypeCode) {
        leaveTypeRepository.findByTenantCodeIgnoreCaseAndCodeIgnoreCase(tenant, leaveTypeCode)
            .orElseThrow(() -> new LeaveResourceNotFoundException("Leave type not found: " + leaveTypeCode));
    }

    private boolean isAdmin(final AuthenticatedLeaveUser actor) {
        return hasRole(actor, "PLATFORM_ADMIN") || hasRole(actor, "TENANT_ADMIN") || hasRole(actor, "HR_ADMIN");
    }

    private void requireAdmin(final AuthenticatedLeaveUser actor) {
        if (!isAdmin(actor)) {
            throw new LeaveForbiddenException("User does not have leave administration permission");
        }
    }

    private boolean hasRole(final AuthenticatedLeaveUser actor, final String role) {
        return actor.roles().contains(role) || actor.roles().contains("ROLE_" + role);
    }

    private boolean requestingOwnEmployee(final AuthenticatedLeaveUser actor, final String employeeId) {
        if (employeeId == null || employeeId.isBlank()) {
            return false;
        }
        // Look up the employee and compare their userAccountId against the actor's auth userId.
        // This is the correct comparison: employeeId is the HR business ID, userId is the auth UUID.
        return employeeRepository.findByTenantCodeIgnoreCaseAndUserAccountId(
            actor.tenantCode(),
            actor.userId().toString()
        ).map(emp -> emp.getId().equalsIgnoreCase(employeeId)).orElse(false);
    }

    private void verifyTenant(final AuthenticatedLeaveUser actor, final String tenantCode) {
        if (!actor.tenantCode().equalsIgnoreCase(tenantCode)) {
            throw new LeaveBusinessException("Token tenant does not match requested tenant");
        }
    }

    private LeaveType toLeaveType(final LeaveTypeEntity entity) {
        return new LeaveType(
            entity.getId(),
            entity.getTenantCode(),
            entity.getCode(),
            entity.getName(),
            entity.isPaid(),
            entity.getDefaultAnnualQuota(),
            entity.isActive(),
            entity.getUpdatedAt(),
            entity.getUpdatedBy()
        );
    }

    private Holiday toHoliday(final HolidayEntity entity) {
        return new Holiday(
            entity.getId(),
            entity.getTenantCode(),
            entity.getHolidayDate(),
            entity.getName(),
            entity.getLocationCode(),
            entity.isActive(),
            entity.getUpdatedAt(),
            entity.getUpdatedBy()
        );
    }

    private LeaveBalance toLeaveBalance(final LeaveBalanceEntity entity) {
        return new LeaveBalance(
            entity.getId(),
            entity.getTenantCode(),
            entity.getEmployeeId(),
            entity.getLeaveTypeCode(),
            entity.getOpeningBalance(),
            entity.getAccruedBalance(),
            entity.getUsedBalance(),
            entity.getAdjustedBalance(),
            available(entity),
            entity.getUpdatedAt(),
            entity.getUpdatedBy()
        );
    }

    private LeaveRequestView toLeaveRequest(final LeaveRequestEntity entity) {
        return new LeaveRequestView(
            entity.getId(),
            entity.getTenantCode(),
            entity.getEmployeeId(),
            entity.getLeaveTypeCode(),
            entity.getStartDate(),
            entity.getEndDate(),
            entity.getTotalDays(),
            entity.getReason(),
            entity.getStatus(),
            entity.getApproverUserId(),
            entity.getApproverEmail(),
            entity.getDecisionComment(),
            entity.getUpdatedAt(),
            entity.getUpdatedBy()
        );
    }

    private BigDecimal available(final LeaveBalanceEntity entity) {
        return amount(entity.getOpeningBalance().add(entity.getAccruedBalance()).add(entity.getAdjustedBalance()).subtract(entity.getUsedBalance()));
    }

    private BigDecimal amount(final BigDecimal value) {
        return (value == null ? BigDecimal.ZERO : value).setScale(2, RoundingMode.HALF_UP);
    }

    private String normTenant(final String tenantCode) {
        return trim(tenantCode).toUpperCase();
    }

    private String trim(final String value) {
        return value == null ? null : value.trim();
    }

    private String blankToNull(final String value) {
        String trimmed = trim(value);
        return trimmed == null || trimmed.isBlank() ? null : trimmed;
    }

    private String blankToNullUpper(final String value) {
        String trimmed = blankToNull(value);
        return trimmed == null ? null : trimmed.toUpperCase();
    }
}

