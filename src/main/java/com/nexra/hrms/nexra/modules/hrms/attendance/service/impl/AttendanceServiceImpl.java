package com.nexra.hrms.nexra.modules.hrms.attendance.service.impl;

import com.nexra.hrms.nexra.common.audit.AuditEventRecord;
import com.nexra.hrms.nexra.common.audit.AuditEventService;
import com.nexra.hrms.nexra.common.workflow.WorkflowRuntime;
import com.nexra.hrms.nexra.modules.hrms.attendance.dto.request.AttendanceRegularizationDecisionRequest;
import com.nexra.hrms.nexra.modules.hrms.attendance.dto.request.AttendanceRegularizationSubmitRequest;
import com.nexra.hrms.nexra.modules.hrms.attendance.dto.request.CheckInRequest;
import com.nexra.hrms.nexra.modules.hrms.attendance.dto.request.CheckOutRequest;
import com.nexra.hrms.nexra.modules.hrms.attendance.dto.request.ShiftUpsertRequest;
import com.nexra.hrms.nexra.modules.hrms.attendance.entity.AttendanceRegularizationEntity;
import com.nexra.hrms.nexra.modules.hrms.attendance.entity.AttendanceRecordEntity;
import com.nexra.hrms.nexra.modules.hrms.attendance.entity.ShiftEntity;
import com.nexra.hrms.nexra.modules.hrms.attendance.exception.AttendanceBusinessException;
import com.nexra.hrms.nexra.modules.hrms.attendance.exception.AttendanceForbiddenException;
import com.nexra.hrms.nexra.modules.hrms.attendance.exception.AttendanceResourceNotFoundException;
import com.nexra.hrms.nexra.modules.hrms.attendance.model.AttendanceRegularizationView;
import com.nexra.hrms.nexra.modules.hrms.attendance.model.AttendanceRecordView;
import com.nexra.hrms.nexra.modules.hrms.attendance.model.ShiftView;
import com.nexra.hrms.nexra.modules.hrms.attendance.repository.AttendanceRegularizationRepository;
import com.nexra.hrms.nexra.modules.hrms.attendance.repository.AttendanceRecordRepository;
import com.nexra.hrms.nexra.modules.hrms.attendance.repository.ShiftRepository;
import com.nexra.hrms.nexra.modules.hrms.attendance.security.AuthenticatedAttendanceUser;
import com.nexra.hrms.nexra.modules.hrms.attendance.service.AttendanceService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implements tenant-scoped attendance and shift management workflows.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceServiceImpl implements AttendanceService {

    private static final String STATUS_CHECKED_IN = "CHECKED_IN";
    private static final String STATUS_PRESENT = "PRESENT";
    private static final String STATUS_PARTIAL = "PARTIAL";
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_APPROVED = "APPROVED";
    private static final String STATUS_REJECTED = "REJECTED";
    private static final String WORKFLOW_PRODUCT_CODE = "HRMS";
    private static final String WORKFLOW_MODULE_KEY = "attendance-regularizations";

    private final ShiftRepository shiftRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final AttendanceRegularizationRepository regularizationRepository;
    private final AuditEventService auditEventService;
    private final WorkflowRuntime workflowRuntime;

    /**
     * Creates or updates a shift definition.
     *
     * @param request shift payload
     * @param actor authenticated user
     * @return saved shift
     */
    @Override
    @Transactional
    public ShiftView upsertShift(final ShiftUpsertRequest request, final AuthenticatedAttendanceUser actor) {
        verifyTenant(actor, request.tenantCode());
        String tenant = normTenant(request.tenantCode());
        String code = trim(request.code()).toUpperCase();
        log.info("AttendanceServiceImpl - upsertShift - tenantCode={}, code={}, actor={}", tenant, code, actor.email());

        ShiftEntity entity = shiftRepository.findByTenantCodeIgnoreCaseAndCodeIgnoreCase(tenant, code).orElseGet(ShiftEntity::new);
        if (entity.getId() == null) {
            entity.setId(UUID.randomUUID().toString());
            entity.setCreatedBy(actor.email());
        }
        entity.setTenantCode(tenant);
        entity.setCode(code);
        entity.setName(trim(request.name()));
        entity.setStartTime(trim(request.startTime()));
        entity.setEndTime(trim(request.endTime()));
        entity.setGraceMinutes(request.graceMinutes());
        entity.setActive(request.active() == null || request.active());
        entity.setUpdatedBy(actor.email());
        ShiftEntity saved = shiftRepository.save(entity);
        recordAudit(saved.getTenantCode(), "UPSERT_SHIFT", actor, "SHIFT", saved.getCode());
        return toModel(saved);
    }

    /**
     * Lists shifts for a tenant.
     *
     * @param tenantCode tenant code
     * @param includeInactive include inactive shifts when true
     * @param actor authenticated user
     * @return shift list
     */
    @Override
    public List<ShiftView> listShifts(final String tenantCode, final boolean includeInactive, final AuthenticatedAttendanceUser actor) {
        verifyTenant(actor, tenantCode);
        log.debug("AttendanceServiceImpl - listShifts - tenantCode={}, includeInactive={}", tenantCode, includeInactive);
        return shiftRepository.findByTenantCodeIgnoreCaseOrderByCodeAsc(normTenant(tenantCode)).stream()
            .filter(shift -> includeInactive || shift.isActive())
            .map(this::toModel)
            .toList();
    }

    /**
     * Records employee check-in for a work date.
     *
     * @param request check-in payload
     * @param actor authenticated user
     * @return saved attendance record
     */
    @Override
    @Transactional
    public AttendanceRecordView checkIn(final CheckInRequest request, final AuthenticatedAttendanceUser actor) {
        verifyTenant(actor, request.tenantCode());
        ensureSelfOrAdmin(actor, request.employeeId());
        String tenant = normTenant(request.tenantCode());
        String shiftCode = blankToNullUpper(request.shiftCode());
        log.info("AttendanceServiceImpl - checkIn - tenantCode={}, employeeId={}, workDate={}, shiftCode={}",
            tenant, request.employeeId(), request.workDate(), shiftCode);

        if (shiftCode != null) {
            shiftRepository.findByTenantCodeIgnoreCaseAndCodeIgnoreCase(tenant, shiftCode)
                .orElseThrow(() -> new AttendanceResourceNotFoundException("Shift not found: " + shiftCode));
        }

        AttendanceRecordEntity entity = attendanceRecordRepository
            .findByTenantCodeIgnoreCaseAndEmployeeIdAndWorkDate(tenant, request.employeeId(), request.workDate())
            .orElseGet(AttendanceRecordEntity::new);

        if (entity.getId() == null) {
            entity.setId(UUID.randomUUID().toString());
            entity.setCreatedBy(actor.email());
            entity.setTenantCode(tenant);
            entity.setEmployeeId(trim(request.employeeId()));
            entity.setWorkDate(request.workDate());
            entity.setTotalHours(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
            entity.setStatus(STATUS_CHECKED_IN);
        }

        if (entity.getCheckInAt() != null) {
            throw new AttendanceBusinessException("Employee already checked in for date: " + request.workDate());
        }

        entity.setShiftCode(shiftCode);
        entity.setCheckInAt(Instant.now());
        entity.setNotes(blankToNull(request.notes()));
        entity.setStatus(STATUS_CHECKED_IN);
        entity.setUpdatedBy(actor.email());
        AttendanceRecordEntity saved = attendanceRecordRepository.save(entity);
        recordAudit(saved.getTenantCode(), "CHECK_IN", actor, "ATTENDANCE_RECORD", saved.getId());
        return toModel(saved);
    }

    /**
     * Completes employee check-out and calculates worked hours.
     *
     * @param request check-out payload
     * @param actor authenticated user
     * @return updated attendance record
     */
    @Override
    @Transactional
    public AttendanceRecordView checkOut(final CheckOutRequest request, final AuthenticatedAttendanceUser actor) {
        verifyTenant(actor, request.tenantCode());
        ensureSelfOrAdmin(actor, request.employeeId());

        AttendanceRecordEntity entity = attendanceRecordRepository.findByTenantCodeIgnoreCaseAndEmployeeIdAndWorkDate(
                normTenant(request.tenantCode()), request.employeeId(), request.workDate())
            .orElseThrow(() -> new AttendanceResourceNotFoundException("Attendance record not found for date: " + request.workDate()));

        if (entity.getCheckInAt() == null) {
            throw new AttendanceBusinessException("Employee is not checked in for date: " + request.workDate());
        }
        if (entity.getCheckOutAt() != null) {
            throw new AttendanceBusinessException("Employee already checked out for date: " + request.workDate());
        }

        Instant checkout = Instant.now();
        if (checkout.isBefore(entity.getCheckInAt())) {
            throw new AttendanceBusinessException("Checkout time cannot be before check-in time");
        }

        BigDecimal hours = BigDecimal.valueOf(Duration.between(entity.getCheckInAt(), checkout).toMinutes())
            .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);

        entity.setCheckOutAt(checkout);
        entity.setTotalHours(hours);
        String notes = blankToNull(request.notes());
        entity.setNotes(notes != null ? notes : entity.getNotes());
        entity.setStatus(hours.compareTo(BigDecimal.ZERO) > 0 ? STATUS_PRESENT : STATUS_PARTIAL);
        entity.setUpdatedBy(actor.email());
        AttendanceRecordEntity saved = attendanceRecordRepository.save(entity);
        recordAudit(saved.getTenantCode(), "CHECK_OUT", actor, "ATTENDANCE_RECORD", saved.getId());
        return toModel(saved);
    }

    /**
     * Lists attendance records in the requested date range and optional employee scope.
     *
     * @param tenantCode tenant code
     * @param employeeId optional employee id
     * @param fromDate optional from date
     * @param toDate optional to date
     * @param actor authenticated user
     * @return attendance records
     */
    @Override
    public List<AttendanceRecordView> listRecords(
        final String tenantCode,
        final String employeeId,
        final LocalDate fromDate,
        final LocalDate toDate,
        final AuthenticatedAttendanceUser actor
    ) {
        verifyTenant(actor, tenantCode);
        LocalDate from = fromDate == null ? LocalDate.now().minusDays(30) : fromDate;
        LocalDate to = toDate == null ? LocalDate.now() : toDate;
        if (to.isBefore(from)) {
            throw new AttendanceBusinessException("toDate must be on or after fromDate");
        }

        String employeeFilter = blankToNull(employeeId);
        if (employeeFilter != null) {
            ensureSelfOrAdmin(actor, employeeFilter);
        }

        log.debug("AttendanceServiceImpl - listRecords - tenantCode={}, employeeId={}, fromDate={}, toDate={}",
            tenantCode, employeeFilter, from, to);

        return (employeeFilter == null
            ? attendanceRecordRepository.findByTenantCodeIgnoreCaseAndWorkDateBetweenOrderByWorkDateAsc(normTenant(tenantCode), from, to)
            : attendanceRecordRepository.findByTenantCodeIgnoreCaseAndEmployeeIdAndWorkDateBetweenOrderByWorkDateAsc(
                normTenant(tenantCode), employeeFilter, from, to))
            .stream()
            .map(this::toModel)
            .toList();
    }

    @Override
    public com.nexra.hrms.nexra.common.api.PageResponse<AttendanceRecordView> listRecords(
        final String tenantCode,
        final String employeeId,
        final LocalDate fromDate,
        final LocalDate toDate,
        final AuthenticatedAttendanceUser actor,
        final org.springframework.data.domain.Pageable pageable
    ) {
        verifyTenant(actor, tenantCode);
        LocalDate from = fromDate == null ? LocalDate.now().minusDays(30) : fromDate;
        LocalDate to = toDate == null ? LocalDate.now() : toDate;
        if (to.isBefore(from)) {
            throw new AttendanceBusinessException("toDate must be on or after fromDate");
        }
        String employeeFilter = blankToNull(employeeId);
        if (employeeFilter != null) {
            ensureSelfOrAdmin(actor, employeeFilter);
        }
        org.springframework.data.domain.Page<com.nexra.hrms.nexra.modules.hrms.attendance.entity.AttendanceRecordEntity> page =
            employeeFilter == null
                ? attendanceRecordRepository.findByTenantCodeIgnoreCaseAndWorkDateBetween(normTenant(tenantCode), from, to, pageable)
                : attendanceRecordRepository.findByTenantCodeIgnoreCaseAndEmployeeIdAndWorkDateBetween(normTenant(tenantCode), employeeFilter, from, to, pageable);
        return com.nexra.hrms.nexra.common.api.PageResponse.map(
            com.nexra.hrms.nexra.common.api.PageResponse.from(page), this::toModel
        );
    }

    /**
     * Computes summarized attendance metrics for the selected scope.
     *
     * @param tenantCode tenant code
     * @param employeeId optional employee id
     * @param fromDate optional from date
     * @param toDate optional to date
     * @param actor authenticated user
     * @return summary map
     */
    @Override
    public Map<String, Object> summary(
        final String tenantCode,
        final String employeeId,
        final LocalDate fromDate,
        final LocalDate toDate,
        final AuthenticatedAttendanceUser actor
    ) {
        List<AttendanceRecordView> records = listRecords(tenantCode, employeeId, fromDate, toDate, actor);
        long presentDays = records.stream().filter(record -> STATUS_PRESENT.equalsIgnoreCase(record.status())).count();
        long checkedInOnlyDays = records.stream().filter(record -> STATUS_CHECKED_IN.equalsIgnoreCase(record.status())).count();
        BigDecimal totalHours = records.stream()
            .map(AttendanceRecordView::totalHours)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("tenantCode", normTenant(tenantCode));
        summary.put("employeeId", blankToNull(employeeId));
        summary.put("recordCount", records.size());
        summary.put("presentDays", presentDays);
        summary.put("checkedInOnlyDays", checkedInOnlyDays);
        summary.put("totalHours", totalHours);
        return summary;
    }

    @Override
    @Transactional
    public AttendanceRegularizationView submitRegularization(
        final AttendanceRegularizationSubmitRequest request,
        final AuthenticatedAttendanceUser actor
    ) {
        verifyTenant(actor, request.tenantCode());
        ensureSelfOrAdmin(actor, request.employeeId());
        final String tenant = normTenant(request.tenantCode());
        if (regularizationRepository.existsByTenantCodeIgnoreCaseAndEmployeeIdAndWorkDateAndStatusIgnoreCase(
            tenant, trim(request.employeeId()), request.workDate(), STATUS_PENDING)) {
            throw new AttendanceBusinessException("A pending regularization already exists for this date.");
        }
        final AttendanceRegularizationEntity entity = new AttendanceRegularizationEntity();
        entity.setId(UUID.randomUUID().toString());
        entity.setTenantCode(tenant);
        entity.setEmployeeId(trim(request.employeeId()));
        entity.setWorkDate(request.workDate());
        entity.setReason(blankToNull(request.reason()));
        entity.setRequestedCheckInAt(request.requestedCheckInAt());
        entity.setRequestedCheckOutAt(request.requestedCheckOutAt());
        entity.setStatus(STATUS_PENDING);
        entity.setCreatedBy(actor.email());
        entity.setUpdatedBy(actor.email());
        AttendanceRegularizationEntity saved = regularizationRepository.save(entity);
        final WorkflowRuntime.WorkflowSubmissionResult workflow = workflowRuntime.submit(
            saved.getTenantCode(),
            WORKFLOW_PRODUCT_CODE,
            WORKFLOW_MODULE_KEY,
            "ATTENDANCE_REGULARIZATION_SUBMITTED",
            actor.email(),
            Map.of("regularizationId", saved.getId(), "employeeId", saved.getEmployeeId())
        );
        saved.setWorkflowInstanceId(workflow.workflowRef());
        saved = regularizationRepository.save(saved);
        recordAudit(saved.getTenantCode(), "SUBMIT_ATTENDANCE_REGULARIZATION", actor, "ATTENDANCE_REGULARIZATION", saved.getId());
        return toRegularization(saved);
    }

    @Override
    @Transactional
    public AttendanceRegularizationView approveRegularization(
        final String requestId,
        final AttendanceRegularizationDecisionRequest request,
        final AuthenticatedAttendanceUser actor
    ) {
        return decideRegularization(requestId, request, actor, true);
    }

    @Override
    @Transactional
    public AttendanceRegularizationView rejectRegularization(
        final String requestId,
        final AttendanceRegularizationDecisionRequest request,
        final AuthenticatedAttendanceUser actor
    ) {
        return decideRegularization(requestId, request, actor, false);
    }

    private AttendanceRegularizationView decideRegularization(
        final String requestId,
        final AttendanceRegularizationDecisionRequest request,
        final AuthenticatedAttendanceUser actor,
        final boolean approve
    ) {
        verifyTenant(actor, request.tenantCode());
        requireAdmin(actor);
        final String tenant = normTenant(request.tenantCode());
        AttendanceRegularizationEntity entity = regularizationRepository.findByIdAndTenantCodeIgnoreCase(requestId, tenant)
            .orElseThrow(() -> new AttendanceResourceNotFoundException("Regularization request not found: " + requestId));
        if (!STATUS_PENDING.equalsIgnoreCase(entity.getStatus())) {
            throw new AttendanceBusinessException("Regularization request already decided: " + entity.getStatus());
        }
        entity.setStatus(approve ? STATUS_APPROVED : STATUS_REJECTED);
        entity.setApproverEmail(actor.email());
        entity.setDecisionComment(blankToNull(request.decisionComment()));
        entity.setUpdatedBy(actor.email());
        if (approve) {
            applyRegularizationToRecord(entity, actor.email());
        }
        AttendanceRegularizationEntity saved = regularizationRepository.save(entity);
        recordAudit(saved.getTenantCode(), approve ? "APPROVE_ATTENDANCE_REGULARIZATION" : "REJECT_ATTENDANCE_REGULARIZATION",
            actor, "ATTENDANCE_REGULARIZATION", saved.getId());
        if (saved.getWorkflowInstanceId() != null) {
            workflowRuntime.advance(saved.getTenantCode(), saved.getWorkflowInstanceId(), approve, actor.email(),
                blankToNull(request.decisionComment()));
        }
        return toRegularization(saved);
    }

    private void applyRegularizationToRecord(final AttendanceRegularizationEntity regularization, final String actorEmail) {
        AttendanceRecordEntity record = attendanceRecordRepository
            .findByTenantCodeIgnoreCaseAndEmployeeIdAndWorkDate(
                regularization.getTenantCode(), regularization.getEmployeeId(), regularization.getWorkDate())
            .orElseGet(AttendanceRecordEntity::new);
        if (record.getId() == null) {
            record.setId(UUID.randomUUID().toString());
            record.setTenantCode(regularization.getTenantCode());
            record.setEmployeeId(regularization.getEmployeeId());
            record.setWorkDate(regularization.getWorkDate());
            record.setCreatedBy(actorEmail);
        }
        if (regularization.getRequestedCheckInAt() != null) {
            record.setCheckInAt(regularization.getRequestedCheckInAt());
        }
        if (regularization.getRequestedCheckOutAt() != null) {
            record.setCheckOutAt(regularization.getRequestedCheckOutAt());
        }
        if (record.getCheckInAt() != null && record.getCheckOutAt() != null) {
            BigDecimal hours = BigDecimal.valueOf(Duration.between(record.getCheckInAt(), record.getCheckOutAt()).toMinutes())
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
            record.setTotalHours(hours);
            record.setStatus(hours.compareTo(BigDecimal.ZERO) > 0 ? STATUS_PRESENT : STATUS_PARTIAL);
        } else if (record.getCheckInAt() != null) {
            record.setStatus(STATUS_CHECKED_IN);
            record.setTotalHours(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        }
        record.setUpdatedBy(actorEmail);
        attendanceRecordRepository.save(record);
    }

    private AttendanceRegularizationView toRegularization(final AttendanceRegularizationEntity entity) {
        return new AttendanceRegularizationView(
            entity.getId(),
            entity.getTenantCode(),
            entity.getEmployeeId(),
            entity.getWorkDate(),
            entity.getReason(),
            entity.getRequestedCheckInAt(),
            entity.getRequestedCheckOutAt(),
            entity.getStatus(),
            entity.getApproverEmail(),
            entity.getDecisionComment()
        );
    }

    private ShiftView toModel(final ShiftEntity entity) {
        return new ShiftView(
            entity.getId(),
            entity.getTenantCode(),
            entity.getCode(),
            entity.getName(),
            entity.getStartTime(),
            entity.getEndTime(),
            entity.getGraceMinutes(),
            entity.isActive(),
            entity.getUpdatedAt(),
            entity.getUpdatedBy()
        );
    }

    private AttendanceRecordView toModel(final AttendanceRecordEntity entity) {
        return new AttendanceRecordView(
            entity.getId(),
            entity.getTenantCode(),
            entity.getEmployeeId(),
            entity.getWorkDate(),
            entity.getShiftCode(),
            entity.getCheckInAt(),
            entity.getCheckOutAt(),
            amount(entity.getTotalHours()),
            entity.getStatus(),
            entity.getNotes(),
            entity.getUpdatedAt(),
            entity.getUpdatedBy()
        );
    }

    private void ensureSelfOrAdmin(final AuthenticatedAttendanceUser actor, final String employeeId) {
        if (actor.userId().toString().equalsIgnoreCase(trim(employeeId)) || isAdmin(actor)) {
            return;
        }
        throw new AttendanceForbiddenException("User cannot access attendance for another employee");
    }

    private void requireAdmin(final AuthenticatedAttendanceUser actor) {
        if (isAdmin(actor)) {
            return;
        }
        throw new AttendanceForbiddenException("User does not have attendance administration permission");
    }

    private boolean isAdmin(final AuthenticatedAttendanceUser actor) {
        return hasRole(actor, "PLATFORM_ADMIN") || hasRole(actor, "TENANT_ADMIN") || hasRole(actor, "HR_ADMIN");
    }

    private boolean hasRole(final AuthenticatedAttendanceUser actor, final String role) {
        return actor.roles().contains(role) || actor.roles().contains("ROLE_" + role);
    }

    private void verifyTenant(final AuthenticatedAttendanceUser actor, final String tenantCode) {
        if (!actor.tenantCode().equalsIgnoreCase(tenantCode)) {
            throw new AttendanceBusinessException("Token tenant does not match requested tenant");
        }
    }

    private void recordAudit(
        final String tenantCode,
        final String action,
        final AuthenticatedAttendanceUser actor,
        final String targetType,
        final String targetId
    ) {
        auditEventService.record(AuditEventRecord.of(tenantCode, "ATTENDANCE", action, "SUCCESS")
            .withActor(actor.email(), actor.userId().toString())
            .withTarget(targetType, targetId));
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

