package com.nexra.hrms.nexra.modules.hrms.timesheet.controller;

import com.nexra.hrms.nexra.common.api.ApiResponse;
import com.nexra.hrms.nexra.modules.hrms.timesheet.dto.request.ProjectUpsertRequest;
import com.nexra.hrms.nexra.modules.hrms.timesheet.dto.request.TimesheetDecisionRequest;
import com.nexra.hrms.nexra.modules.hrms.timesheet.dto.request.TimesheetEntryCreateRequest;
import com.nexra.hrms.nexra.modules.hrms.timesheet.exception.TimesheetForbiddenException;
import com.nexra.hrms.nexra.modules.hrms.timesheet.exception.TimesheetUnauthorizedException;
import com.nexra.hrms.nexra.modules.hrms.timesheet.model.ProjectView;
import com.nexra.hrms.nexra.modules.hrms.timesheet.model.TimesheetEntryView;
import com.nexra.hrms.nexra.modules.hrms.timesheet.security.AuthenticatedTimesheetUser;
import com.nexra.hrms.nexra.modules.hrms.timesheet.security.TimesheetAuthFilter;
import com.nexra.hrms.nexra.modules.hrms.timesheet.service.TimesheetService;
import com.nexra.hrms.nexra.modules.hrms.employee.validation.TenantCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.time.LocalDate;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Exposes tenant-scoped timesheet APIs for projects, entries, approvals, and summary.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@Tag(name = "Timesheet", description = "Timesheet APIs.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/timesheet")
@Slf4j
@Validated
public class TimesheetController {

    private final TimesheetService timesheetService;

    /**
     * Returns service availability metadata.
     *
     * @return status payload
     */
    @Operation(summary = "GET endpoint", description = "Handles GET requests for this resource.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> status() {
        return ResponseEntity.ok(ApiResponse.success("timesheet service is available.", Map.of(
            "service", "timesheet",
            "timestamp", Instant.now().toString(),
            "state", "UP"
        )));
    }

    /**
     * Returns capabilities metadata for client discovery.
     *
     * @return capabilities payload
     */
    @Operation(summary = "GET endpoint", description = "Handles GET requests for this resource.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
    @GetMapping("/capabilities")
    public ResponseEntity<ApiResponse<Map<String, Object>>> capabilities() {
        return ResponseEntity.ok(ApiResponse.success("timesheet capabilities fetched successfully.", Map.of(
            "domains", List.of("projects", "entries", "approvals", "summary"),
            "storage", "MySQL + Flyway",
            "auth", "JWT tenant-scoped"
        )));
    }

    /**
     * Creates or updates a project.
     *
     * @param request project payload
     * @param httpRequest servlet request
     * @return project response
     */
    @Operation(summary = "PUT endpoint", description = "Handles PUT requests for this resource.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
    @PutMapping("/projects")
    public ResponseEntity<ApiResponse<ProjectView>> upsertProject(
        @Valid @RequestBody final ProjectUpsertRequest request,
        final HttpServletRequest httpRequest
    ) {
        AuthenticatedTimesheetUser actor = currentUser(httpRequest);
        requireAdmin(actor);
        log.info("TimesheetController - upsertProject - tenantCode={}, projectCode={}", request.tenantCode(), request.projectCode());
        return ResponseEntity.ok(ApiResponse.success(
            "Project saved successfully.",
            timesheetService.upsertProject(request, actor)
        ));
    }

    /**
     * Lists projects for a tenant.
     *
     * @param tenantCode tenant code
     * @param includeInactive include inactive projects when true
     * @param httpRequest servlet request
     * @return project list response
     */
    @Operation(summary = "GET endpoint", description = "Handles GET requests for this resource.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
    @GetMapping("/projects")
    public ResponseEntity<ApiResponse<List<ProjectView>>> listProjects(
        @RequestParam @NotBlank @TenantCode @Size(max = 60) final String tenantCode,
        @RequestParam(defaultValue = "false") final boolean includeInactive,
        final HttpServletRequest httpRequest
    ) {
        log.debug("TimesheetController - listProjects - tenantCode={}, includeInactive={}", tenantCode, includeInactive);
        return ResponseEntity.ok(ApiResponse.success(
            "Projects fetched successfully.",
            timesheetService.listProjects(tenantCode, includeInactive, currentUser(httpRequest))
        ));
    }

    /**
     * Creates a new timesheet entry.
     *
     * @param request timesheet entry payload
     * @param httpRequest servlet request
     * @return created entry response
     */
    @Operation(summary = "POST endpoint", description = "Handles POST requests for this resource.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
    @PostMapping("/entries")
    public ResponseEntity<ApiResponse<TimesheetEntryView>> createEntry(
        @Valid @RequestBody final TimesheetEntryCreateRequest request,
        final HttpServletRequest httpRequest
    ) {
        log.info("TimesheetController - createEntry - tenantCode={}, employeeId={}, projectCode={}",
            request.tenantCode(), request.employeeId(), request.projectCode());
        return ResponseEntity.ok(ApiResponse.success(
            "Timesheet entry created successfully.",
            timesheetService.createEntry(request, currentUser(httpRequest))
        ));
    }

    /**
     * Lists entries for a tenant employee and date range.
     *
     * @param tenantCode tenant code
     * @param employeeId employee id
     * @param fromDate optional from date
     * @param toDate optional to date
     * @param httpRequest servlet request
     * @return entries response
     */
    @Operation(summary = "GET endpoint", description = "Handles GET requests for this resource.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
    @GetMapping("/entries")
    public ResponseEntity<ApiResponse<com.nexra.hrms.nexra.common.api.PageResponse<TimesheetEntryView>>> listEntries(
        @RequestParam @NotBlank @TenantCode @Size(max = 60) final String tenantCode,
        @RequestParam final String employeeId,
        @RequestParam(required = false) final LocalDate fromDate,
        @RequestParam(required = false) final LocalDate toDate,
        @RequestParam(defaultValue = "0") @Min(0) final int page,
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) final int size,
        final HttpServletRequest httpRequest
    ) {
        log.debug("TimesheetController - listEntries - tenantCode={}, employeeId={}, fromDate={}, toDate={}",
            tenantCode, employeeId, fromDate, toDate);
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(
            page, Math.min(size, 100), org.springframework.data.domain.Sort.by("workDate").descending()
        );
        return ResponseEntity.ok(ApiResponse.success(
            "Timesheet entries fetched successfully.",
            timesheetService.listEntries(tenantCode, employeeId, fromDate, toDate, currentUser(httpRequest), pageable)
        ));
    }

    /**
     * Returns a single timesheet entry.
     *
     * @param entryId entry id
     * @param tenantCode tenant code
     * @param httpRequest servlet request
     * @return entry response
     */
    @Operation(summary = "GET endpoint", description = "Handles GET requests for this resource.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
    @GetMapping("/entries/{entryId}")
    public ResponseEntity<ApiResponse<TimesheetEntryView>> getEntry(
        @PathVariable final String entryId,
        @RequestParam @NotBlank @TenantCode @Size(max = 60) final String tenantCode,
        final HttpServletRequest httpRequest
    ) {
        return ResponseEntity.ok(ApiResponse.success(
            "Timesheet entry fetched successfully.",
            timesheetService.getEntry(tenantCode, entryId, currentUser(httpRequest))
        ));
    }

    /**
     * Approves a submitted timesheet entry.
     *
     * @param entryId entry id
     * @param request approval payload
     * @param httpRequest servlet request
     * @return approved entry response
     */
    @Operation(summary = "POST endpoint", description = "Handles POST requests for this resource.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
    @PostMapping("/entries/{entryId}/approve")
    public ResponseEntity<ApiResponse<TimesheetEntryView>> approve(
        @PathVariable final String entryId,
        @Valid @RequestBody final TimesheetDecisionRequest request,
        final HttpServletRequest httpRequest
    ) {
        log.info("TimesheetController - approve - entryId={}, tenantCode={}", entryId, request.tenantCode());
        return ResponseEntity.ok(ApiResponse.success(
            "Timesheet entry approved successfully.",
            timesheetService.approveEntry(entryId, request, currentUser(httpRequest))
        ));
    }

    /**
     * Rejects a submitted timesheet entry.
     *
     * @param entryId entry id
     * @param request rejection payload
     * @param httpRequest servlet request
     * @return rejected entry response
     */
    @Operation(summary = "POST endpoint", description = "Handles POST requests for this resource.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
    @PostMapping("/entries/{entryId}/reject")
    public ResponseEntity<ApiResponse<TimesheetEntryView>> reject(
        @PathVariable final String entryId,
        @Valid @RequestBody final TimesheetDecisionRequest request,
        final HttpServletRequest httpRequest
    ) {
        log.info("TimesheetController - reject - entryId={}, tenantCode={}", entryId, request.tenantCode());
        return ResponseEntity.ok(ApiResponse.success(
            "Timesheet entry rejected successfully.",
            timesheetService.rejectEntry(entryId, request, currentUser(httpRequest))
        ));
    }

    /**
     * Returns timesheet summary metrics.
     *
     * @param tenantCode tenant code
     * @param employeeId employee id
     * @param fromDate optional from date
     * @param toDate optional to date
     * @param httpRequest servlet request
     * @return summary response
     */
    @Operation(summary = "GET endpoint", description = "Handles GET requests for this resource.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> summary(
        @RequestParam @NotBlank @TenantCode @Size(max = 60) final String tenantCode,
        @RequestParam final String employeeId,
        @RequestParam(required = false) final LocalDate fromDate,
        @RequestParam(required = false) final LocalDate toDate,
        final HttpServletRequest httpRequest
    ) {
        log.debug("TimesheetController - summary - tenantCode={}, employeeId={}, fromDate={}, toDate={}",
            tenantCode, employeeId, fromDate, toDate);
        return ResponseEntity.ok(ApiResponse.success(
            "Timesheet summary fetched successfully.",
            timesheetService.summary(tenantCode, employeeId, fromDate, toDate, currentUser(httpRequest))
        ));
    }

    private AuthenticatedTimesheetUser currentUser(final HttpServletRequest request) {
        Object value = request.getAttribute(TimesheetAuthFilter.ATTR_AUTH_USER);
        if (value instanceof AuthenticatedTimesheetUser user) {
            return user;
        }
        throw new TimesheetUnauthorizedException("Missing authenticated timesheet user");
    }

    private void requireAdmin(final AuthenticatedTimesheetUser actor) {
        if (hasRole(actor, "PLATFORM_ADMIN") || hasRole(actor, "TENANT_ADMIN") || hasRole(actor, "HR_ADMIN") || hasRole(actor, "MANAGER")) {
            return;
        }
        throw new TimesheetForbiddenException("User does not have timesheet administration permission");
    }

    private boolean hasRole(final AuthenticatedTimesheetUser actor, final String role) {
        return actor.roles().contains(role) || actor.roles().contains("ROLE_" + role);
    }
}

