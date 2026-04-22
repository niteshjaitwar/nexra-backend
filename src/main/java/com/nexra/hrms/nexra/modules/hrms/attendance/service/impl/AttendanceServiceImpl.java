package com.nexra.hrms.nexra.modules.hrms.attendance.service.impl;

import com.nexra.hrms.nexra.modules.hrms.attendance.dto.request.CheckInRequest;
import com.nexra.hrms.nexra.modules.hrms.attendance.dto.request.CheckOutRequest;
import com.nexra.hrms.nexra.modules.hrms.attendance.dto.request.ShiftUpsertRequest;
import com.nexra.hrms.nexra.modules.hrms.attendance.entity.AttendanceRecordEntity;
import com.nexra.hrms.nexra.modules.hrms.attendance.entity.ShiftEntity;
import com.nexra.hrms.nexra.modules.hrms.attendance.exception.AttendanceBusinessException;
import com.nexra.hrms.nexra.modules.hrms.attendance.exception.AttendanceForbiddenException;
import com.nexra.hrms.nexra.modules.hrms.attendance.exception.AttendanceResourceNotFoundException;
import com.nexra.hrms.nexra.modules.hrms.attendance.model.AttendanceRecordView;
import com.nexra.hrms.nexra.modules.hrms.attendance.model.ShiftView;
import com.nexra.hrms.nexra.modules.hrms.attendance.repository.AttendanceRecordRepository;
import com.nexra.hrms.nexra.modules.hrms.attendance.repository.ShiftRepository;
import com.nexra.hrms.nexra.modules.hrms.attendance.security.AuthenticatedAttendanceUser;
import com.nexra.hrms.nexra.modules.hrms.attendance.service.AttendanceService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
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

    private final ShiftRepository shiftRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;

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
        return toModel(shiftRepository.save(entity));
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
        return toModel(attendanceRecordRepository.save(entity));
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
        return toModel(attendanceRecordRepository.save(entity));
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

        return Map.of(
            "tenantCode", normTenant(tenantCode),
            "employeeId", blankToNull(employeeId),
            "recordCount", records.size(),
            "presentDays", presentDays,
            "checkedInOnlyDays", checkedInOnlyDays,
            "totalHours", totalHours
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

