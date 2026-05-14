package com.nexra.hrms.nexra.modules.hrms.timesheet.service.impl;

import com.nexra.hrms.nexra.modules.hrms.timesheet.dto.request.ProjectUpsertRequest;
import com.nexra.hrms.nexra.modules.hrms.timesheet.dto.request.TimesheetDecisionRequest;
import com.nexra.hrms.nexra.modules.hrms.timesheet.dto.request.TimesheetEntryCreateRequest;
import com.nexra.hrms.nexra.modules.hrms.timesheet.entity.ProjectEntity;
import com.nexra.hrms.nexra.modules.hrms.timesheet.entity.TimesheetEntryEntity;
import com.nexra.hrms.nexra.modules.hrms.timesheet.exception.TimesheetBusinessException;
import com.nexra.hrms.nexra.modules.hrms.timesheet.exception.TimesheetForbiddenException;
import com.nexra.hrms.nexra.modules.hrms.timesheet.exception.TimesheetResourceNotFoundException;
import com.nexra.hrms.nexra.modules.hrms.timesheet.model.ProjectView;
import com.nexra.hrms.nexra.modules.hrms.timesheet.model.TimesheetEntryView;
import com.nexra.hrms.nexra.modules.hrms.timesheet.repository.ProjectRepository;
import com.nexra.hrms.nexra.modules.hrms.timesheet.repository.TimesheetEntryRepository;
import com.nexra.hrms.nexra.modules.hrms.timesheet.security.AuthenticatedTimesheetUser;
import com.nexra.hrms.nexra.modules.hrms.timesheet.service.TimesheetService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implements tenant-scoped timesheet and project workflows.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TimesheetServiceImpl implements TimesheetService {

    private static final String STATUS_SUBMITTED = "SUBMITTED";
    private static final String STATUS_APPROVED = "APPROVED";
    private static final String STATUS_REJECTED = "REJECTED";

    private final ProjectRepository projectRepository;
    private final TimesheetEntryRepository entryRepository;

    /**
     * Creates or updates a project.
     *
     * @param request project payload
     * @param actor authenticated user
     * @return saved project
     */
    @Override
    @Transactional
    public ProjectView upsertProject(final ProjectUpsertRequest request, final AuthenticatedTimesheetUser actor) {
        verifyTenant(actor, request.tenantCode());
        requireAdmin(actor);
        String tenant = normTenant(request.tenantCode());
        String code = trim(request.projectCode()).toUpperCase();
        log.info("TimesheetServiceImpl - upsertProject - tenantCode={}, projectCode={}, actor={}", tenant, code, actor.email());

        ProjectEntity entity = projectRepository.findByTenantCodeIgnoreCaseAndProjectCodeIgnoreCase(tenant, code)
            .orElseGet(ProjectEntity::new);
        if (entity.getId() == null) {
            entity.setId(UUID.randomUUID().toString());
            entity.setCreatedBy(actor.email());
        }
        entity.setTenantCode(tenant);
        entity.setProjectCode(code);
        entity.setProjectName(trim(request.projectName()));
        entity.setClientName(blankToNull(request.clientName()));
        entity.setBillable(request.billable());
        entity.setActive(request.active() == null || request.active());
        entity.setUpdatedBy(actor.email());
        return toProject(projectRepository.save(entity));
    }

    /**
     * Lists projects for a tenant.
     *
     * @param tenantCode tenant code
     * @param includeInactive include inactive projects
     * @param actor authenticated user
     * @return project list
     */
    @Override
    public List<ProjectView> listProjects(final String tenantCode, final boolean includeInactive, final AuthenticatedTimesheetUser actor) {
        verifyTenant(actor, tenantCode);
        log.debug("TimesheetServiceImpl - listProjects - tenantCode={}, includeInactive={}", tenantCode, includeInactive);
        return projectRepository.findByTenantCodeIgnoreCaseOrderByProjectCodeAsc(normTenant(tenantCode)).stream()
            .filter(project -> includeInactive || project.isActive())
            .map(this::toProject)
            .toList();
    }

    /**
     * Creates a submitted timesheet entry.
     *
     * @param request entry payload
     * @param actor authenticated user
     * @return created entry
     */
    @Override
    @Transactional
    public TimesheetEntryView createEntry(final TimesheetEntryCreateRequest request, final AuthenticatedTimesheetUser actor) {
        verifyTenant(actor, request.tenantCode());
        ensureSelfOrAdmin(actor, request.employeeId());
        String tenant = normTenant(request.tenantCode());
        log.info("TimesheetServiceImpl - createEntry - tenantCode={}, employeeId={}, projectCode={}, workDate={}",
            tenant, request.employeeId(), request.projectCode(), request.workDate());

        ProjectEntity project = projectRepository.findByTenantCodeIgnoreCaseAndProjectCodeIgnoreCase(tenant, request.projectCode())
            .orElseThrow(() -> new TimesheetResourceNotFoundException("Project not found: " + request.projectCode()));
        if (!project.isActive()) {
            throw new TimesheetBusinessException("Project inactive: " + request.projectCode());
        }

        TimesheetEntryEntity entity = new TimesheetEntryEntity();
        entity.setId(UUID.randomUUID().toString());
        entity.setTenantCode(tenant);
        entity.setEmployeeId(trim(request.employeeId()));
        entity.setWorkDate(request.workDate());
        entity.setProjectCode(project.getProjectCode());
        entity.setTaskName(trim(request.taskName()));
        entity.setHours(amount(request.hours()));
        entity.setBillable(request.billable());
        entity.setStatus(STATUS_SUBMITTED);
        entity.setNotes(blankToNull(request.notes()));
        entity.setCreatedBy(actor.email());
        entity.setUpdatedBy(actor.email());
        return toEntry(entryRepository.save(entity));
    }

    /**
     * Lists employee entries in the requested date range.
     *
     * @param tenantCode tenant code
     * @param employeeId employee id
     * @param fromDate optional from date
     * @param toDate optional to date
     * @param actor authenticated user
     * @return entry list
     */
    @Override
    public List<TimesheetEntryView> listEntries(
        final String tenantCode,
        final String employeeId,
        final LocalDate fromDate,
        final LocalDate toDate,
        final AuthenticatedTimesheetUser actor
    ) {
        verifyTenant(actor, tenantCode);
        String employee = trim(employeeId);
        ensureSelfOrAdmin(actor, employee);
        LocalDate from = fromDate == null ? LocalDate.now().minusDays(30) : fromDate;
        LocalDate to = toDate == null ? LocalDate.now() : toDate;
        if (to.isBefore(from)) {
            throw new TimesheetBusinessException("toDate must be on or after fromDate");
        }
        log.debug("TimesheetServiceImpl - listEntries - tenantCode={}, employeeId={}, fromDate={}, toDate={}",
            tenantCode, employee, from, to);
        return entryRepository
            .findByTenantCodeIgnoreCaseAndEmployeeIdAndWorkDateBetweenOrderByWorkDateAsc(normTenant(tenantCode), employee, from, to)
            .stream()
            .map(this::toEntry)
            .toList();
    }

    @Override
    public com.nexra.hrms.nexra.common.api.PageResponse<TimesheetEntryView> listEntries(
        final String tenantCode,
        final String employeeId,
        final LocalDate fromDate,
        final LocalDate toDate,
        final AuthenticatedTimesheetUser actor,
        final org.springframework.data.domain.Pageable pageable
    ) {
        verifyTenant(actor, tenantCode);
        String employee = trim(employeeId);
        ensureSelfOrAdmin(actor, employee);
        LocalDate from = fromDate == null ? LocalDate.now().minusDays(30) : fromDate;
        LocalDate to = toDate == null ? LocalDate.now() : toDate;
        if (to.isBefore(from)) {
            throw new TimesheetBusinessException("toDate must be on or after fromDate");
        }
        org.springframework.data.domain.Page<TimesheetEntryEntity> page =
            entryRepository.findByTenantCodeIgnoreCaseAndEmployeeIdAndWorkDateBetween(normTenant(tenantCode), employee, from, to, pageable);
        return com.nexra.hrms.nexra.common.api.PageResponse.map(
            com.nexra.hrms.nexra.common.api.PageResponse.from(page), this::toEntry
        );
    }

    /**
     * Gets a single timesheet entry.
     *
     * @param tenantCode tenant code
     * @param entryId entry id
     * @param actor authenticated user
     * @return entry view
     */
    @Override
    public TimesheetEntryView getEntry(final String tenantCode, final String entryId, final AuthenticatedTimesheetUser actor) {
        verifyTenant(actor, tenantCode);
        TimesheetEntryEntity entry = entryRepository.findByIdAndTenantCodeIgnoreCase(entryId, normTenant(tenantCode))
            .orElseThrow(() -> new TimesheetResourceNotFoundException("Timesheet entry not found: " + entryId));
        ensureSelfOrAdmin(actor, entry.getEmployeeId());
        return toEntry(entry);
    }

    /**
     * Approves a submitted timesheet entry.
     *
     * @param entryId entry id
     * @param request approval payload
     * @param actor authenticated user
     * @return updated entry
     */
    @Override
    @Transactional
    public TimesheetEntryView approveEntry(
        final String entryId,
        final TimesheetDecisionRequest request,
        final AuthenticatedTimesheetUser actor
    ) {
        return decide(entryId, request, actor, true);
    }

    /**
     * Rejects a submitted timesheet entry.
     *
     * @param entryId entry id
     * @param request rejection payload
     * @param actor authenticated user
     * @return updated entry
     */
    @Override
    @Transactional
    public TimesheetEntryView rejectEntry(
        final String entryId,
        final TimesheetDecisionRequest request,
        final AuthenticatedTimesheetUser actor
    ) {
        return decide(entryId, request, actor, false);
    }

    /**
     * Computes timesheet summary metrics.
     *
     * @param tenantCode tenant code
     * @param employeeId employee id
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
        final AuthenticatedTimesheetUser actor
    ) {
        List<TimesheetEntryView> entries = listEntries(tenantCode, employeeId, fromDate, toDate, actor);
        BigDecimal totalHours = entries.stream()
            .map(TimesheetEntryView::hours)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);
        BigDecimal billableHours = entries.stream()
            .filter(TimesheetEntryView::billable)
            .map(TimesheetEntryView::hours)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);
        return Map.of(
            "tenantCode", normTenant(tenantCode),
            "employeeId", trim(employeeId),
            "entryCount", entries.size(),
            "totalHours", totalHours,
            "billableHours", billableHours
        );
    }

    private TimesheetEntryView decide(
        final String entryId,
        final TimesheetDecisionRequest request,
        final AuthenticatedTimesheetUser actor,
        final boolean approve
    ) {
        verifyTenant(actor, request.tenantCode());
        requireAdmin(actor);
        TimesheetEntryEntity entry = entryRepository.findByIdAndTenantCodeIgnoreCase(entryId, normTenant(request.tenantCode()))
            .orElseThrow(() -> new TimesheetResourceNotFoundException("Timesheet entry not found: " + entryId));
        if (!STATUS_SUBMITTED.equalsIgnoreCase(entry.getStatus())) {
            throw new TimesheetBusinessException("Timesheet entry already decided: " + entry.getStatus());
        }
        entry.setStatus(approve ? STATUS_APPROVED : STATUS_REJECTED);
        entry.setApproverUserId(actor.userId().toString());
        entry.setApproverEmail(actor.email());
        entry.setApprovalComment(blankToNull(request.comment()));
        entry.setUpdatedBy(actor.email());
        log.info("TimesheetServiceImpl - decide - tenantCode={}, entryId={}, action={}, approver={}",
            request.tenantCode(), entryId, approve ? "APPROVE" : "REJECT", actor.email());
        return toEntry(entryRepository.save(entry));
    }

    private ProjectView toProject(final ProjectEntity entity) {
        return new ProjectView(
            entity.getId(),
            entity.getTenantCode(),
            entity.getProjectCode(),
            entity.getProjectName(),
            entity.getClientName(),
            entity.isBillable(),
            entity.isActive(),
            entity.getUpdatedAt(),
            entity.getUpdatedBy()
        );
    }

    private TimesheetEntryView toEntry(final TimesheetEntryEntity entity) {
        return new TimesheetEntryView(
            entity.getId(),
            entity.getTenantCode(),
            entity.getEmployeeId(),
            entity.getWorkDate(),
            entity.getProjectCode(),
            entity.getTaskName(),
            amount(entity.getHours()),
            entity.isBillable(),
            entity.getStatus(),
            entity.getApproverUserId(),
            entity.getApproverEmail(),
            entity.getApprovalComment(),
            entity.getNotes(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    private void ensureSelfOrAdmin(final AuthenticatedTimesheetUser actor, final String employeeId) {
        if (actor.userId().toString().equalsIgnoreCase(trim(employeeId)) || isAdmin(actor)) {
            return;
        }
        throw new TimesheetForbiddenException("User cannot access another employee timesheet");
    }

    private void requireAdmin(final AuthenticatedTimesheetUser actor) {
        if (!isAdmin(actor)) {
            throw new TimesheetForbiddenException("User does not have timesheet administration permission");
        }
    }

    private boolean isAdmin(final AuthenticatedTimesheetUser actor) {
        return hasRole(actor, "PLATFORM_ADMIN")
            || hasRole(actor, "TENANT_ADMIN")
            || hasRole(actor, "HR_ADMIN")
            || hasRole(actor, "MANAGER");
    }

    private boolean hasRole(final AuthenticatedTimesheetUser actor, final String role) {
        return actor.roles().contains(role) || actor.roles().contains("ROLE_" + role);
    }

    private void verifyTenant(final AuthenticatedTimesheetUser actor, final String tenantCode) {
        if (!actor.tenantCode().equalsIgnoreCase(tenantCode)) {
            throw new TimesheetBusinessException("Token tenant does not match requested tenant");
        }
    }

    private BigDecimal amount(final BigDecimal value) {
        return (value == null ? BigDecimal.ZERO : value).setScale(2, RoundingMode.HALF_UP);
    }

    private String normTenant(final String value) {
        return trim(value).toUpperCase();
    }

    private String trim(final String value) {
        return value == null ? null : value.trim();
    }

    private String blankToNull(final String value) {
        String trimmed = trim(value);
        return trimmed == null || trimmed.isBlank() ? null : trimmed;
    }
}

