package com.nexra.hrms.nexra.modules.hrms.attendance.controller;

import com.nexra.hrms.nexra.common.api.ApiResponse;
import com.nexra.hrms.nexra.modules.hrms.attendance.dto.request.CheckInRequest;
import com.nexra.hrms.nexra.modules.hrms.attendance.dto.request.CheckOutRequest;
import com.nexra.hrms.nexra.modules.hrms.attendance.dto.request.ShiftUpsertRequest;
import com.nexra.hrms.nexra.modules.hrms.attendance.exception.AttendanceForbiddenException;
import com.nexra.hrms.nexra.modules.hrms.attendance.exception.AttendanceUnauthorizedException;
import com.nexra.hrms.nexra.modules.hrms.attendance.model.AttendanceRecordView;
import com.nexra.hrms.nexra.modules.hrms.attendance.model.ShiftView;
import com.nexra.hrms.nexra.modules.hrms.attendance.security.AttendanceAuthFilter;
import com.nexra.hrms.nexra.modules.hrms.attendance.security.AuthenticatedAttendanceUser;
import com.nexra.hrms.nexra.modules.hrms.attendance.service.AttendanceService;
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
 * Exposes tenant-scoped attendance and shift management endpoints.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@Tag(name = "Attendance", description = "Attendance APIs.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/attendance")
@Slf4j
@Validated
public class AttendanceController {

    private final AttendanceService attendanceService;

    /**
     * Returns service availability metadata.
     *
     * @return status payload
     */
    @Operation(summary = "GET /api/v1/attendance/status", description = "Processes GET requests for /api/v1/attendance/status.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> status() {
        return ResponseEntity.ok(ApiResponse.success("attendance service is available.", Map.of(
            "service", "attendance", "timestamp", Instant.now().toString(), "state", "UP"
        )));
    }

    /**
     * Returns attendance capabilities for client discovery.
     *
     * @return capabilities payload
     */
    @Operation(summary = "GET /api/v1/attendance/capabilities", description = "Processes GET requests for /api/v1/attendance/capabilities.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
    @GetMapping("/capabilities")
    public ResponseEntity<ApiResponse<Map<String, Object>>> capabilities() {
        return ResponseEntity.ok(ApiResponse.success("attendance capabilities fetched successfully.", Map.of(
            "domains", List.of("shifts", "check-in", "check-out", "records", "summary"),
            "auth", "JWT tenant-scoped",
            "storage", "MySQL + Flyway"
        )));
    }

    /**
     * Creates or updates a shift.
     *
     * @param request shift payload
     * @param httpRequest servlet request
     * @return saved shift
     */
    @Operation(summary = "PUT /api/v1/attendance/shifts", description = "Processes PUT requests for /api/v1/attendance/shifts.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
    @PutMapping("/shifts")
    public ResponseEntity<ApiResponse<ShiftView>> upsertShift(
        @Valid @RequestBody final ShiftUpsertRequest request,
        final HttpServletRequest httpRequest
    ) {
        AuthenticatedAttendanceUser actor = currentUser(httpRequest);
        requireAdmin(actor);
        log.info("AttendanceController - upsertShift - tenantCode={}, actor={}", request.tenantCode(), actor.email());
        return ResponseEntity.ok(ApiResponse.success("Shift saved successfully.", attendanceService.upsertShift(request, actor)));
    }

    /**
     * Lists shifts for a tenant.
     *
     * @param tenantCode tenant code
     * @param includeInactive include inactive rows when true
     * @param httpRequest servlet request
     * @return shift list
     */
    @Operation(summary = "GET /api/v1/attendance/shifts", description = "Processes GET requests for /api/v1/attendance/shifts.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
    @GetMapping("/shifts")
    public ResponseEntity<ApiResponse<List<ShiftView>>> listShifts(
        @RequestParam @NotBlank @TenantCode @Size(max = 60) final String tenantCode,
        @RequestParam(defaultValue = "false") final boolean includeInactive,
        final HttpServletRequest httpRequest
    ) {
        log.debug("AttendanceController - listShifts - tenantCode={}, includeInactive={}", tenantCode, includeInactive);
        return ResponseEntity.ok(ApiResponse.success(
            "Shifts fetched successfully.",
            attendanceService.listShifts(tenantCode, includeInactive, currentUser(httpRequest))
        ));
    }

    /**
     * Checks in an employee.
     *
     * @param request check-in payload
     * @param httpRequest servlet request
     * @return attendance record
     */
    @Operation(summary = "POST /api/v1/attendance/check-in", description = "Processes POST requests for /api/v1/attendance/check-in.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
    @PostMapping("/check-in")
    public ResponseEntity<ApiResponse<AttendanceRecordView>> checkIn(
        @Valid @RequestBody final CheckInRequest request,
        final HttpServletRequest httpRequest
    ) {
        log.info("AttendanceController - checkIn - tenantCode={}, employeeId={}", request.tenantCode(), request.employeeId());
        return ResponseEntity.ok(ApiResponse.success("Check-in successful.", attendanceService.checkIn(request, currentUser(httpRequest))));
    }

    /**
     * Checks out an employee.
     *
     * @param request check-out payload
     * @param httpRequest servlet request
     * @return attendance record
     */
    @Operation(summary = "POST /api/v1/attendance/check-out", description = "Processes POST requests for /api/v1/attendance/check-out.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
    @PostMapping("/check-out")
    public ResponseEntity<ApiResponse<AttendanceRecordView>> checkOut(
        @Valid @RequestBody final CheckOutRequest request,
        final HttpServletRequest httpRequest
    ) {
        log.info("AttendanceController - checkOut - tenantCode={}, employeeId={}", request.tenantCode(), request.employeeId());
        return ResponseEntity.ok(ApiResponse.success("Check-out successful.", attendanceService.checkOut(request, currentUser(httpRequest))));
    }

    /**
     * Lists attendance records with tenant, employee, and date filters.
     *
     * @param tenantCode tenant code
     * @param employeeId optional employee id
     * @param fromDate optional from date
     * @param toDate optional to date
     * @param httpRequest servlet request
     * @return record list
     */
    @Operation(summary = "GET /api/v1/attendance/records", description = "Processes GET requests for /api/v1/attendance/records.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
    @GetMapping("/records")
    public ResponseEntity<ApiResponse<com.nexra.hrms.nexra.common.api.PageResponse<AttendanceRecordView>>> records(
        @RequestParam @NotBlank @TenantCode @Size(max = 60) final String tenantCode,
        @RequestParam(required = false) final String employeeId,
        @RequestParam(required = false) final LocalDate fromDate,
        @RequestParam(required = false) final LocalDate toDate,
        @RequestParam(defaultValue = "0") @Min(0) final int page,
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) final int size,
        final HttpServletRequest httpRequest
    ) {
        log.debug("AttendanceController - records - tenantCode={}, employeeId={}, fromDate={}, toDate={}",
            tenantCode, employeeId, fromDate, toDate);
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(
            page, Math.min(size, 100), org.springframework.data.domain.Sort.by("workDate").descending()
        );
        return ResponseEntity.ok(ApiResponse.success(
            "Attendance records fetched successfully.",
            attendanceService.listRecords(tenantCode, employeeId, fromDate, toDate, currentUser(httpRequest), pageable)
        ));
    }

    /**
     * Returns summarized attendance metrics.
     *
     * @param tenantCode tenant code
     * @param employeeId optional employee id
     * @param fromDate optional from date
     * @param toDate optional to date
     * @param httpRequest servlet request
     * @return summary payload
     */
    @Operation(summary = "GET /api/v1/attendance/summary", description = "Processes GET requests for /api/v1/attendance/summary.")
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
        @RequestParam(required = false) final String employeeId,
        @RequestParam(required = false) final LocalDate fromDate,
        @RequestParam(required = false) final LocalDate toDate,
        final HttpServletRequest httpRequest
    ) {
        log.debug("AttendanceController - summary - tenantCode={}, employeeId={}, fromDate={}, toDate={}",
            tenantCode, employeeId, fromDate, toDate);
        return ResponseEntity.ok(ApiResponse.success(
            "Attendance summary fetched successfully.",
            attendanceService.summary(tenantCode, employeeId, fromDate, toDate, currentUser(httpRequest))
        ));
    }

    private AuthenticatedAttendanceUser currentUser(final HttpServletRequest request) {
        Object value = request.getAttribute(AttendanceAuthFilter.ATTR_AUTH_USER);
        if (value instanceof AuthenticatedAttendanceUser user) {
            return user;
        }
        throw new AttendanceUnauthorizedException("Missing authenticated attendance user");
    }

    private void requireAdmin(final AuthenticatedAttendanceUser actor) {
        if (hasRole(actor, "PLATFORM_ADMIN") || hasRole(actor, "TENANT_ADMIN") || hasRole(actor, "HR_ADMIN")) {
            return;
        }
        throw new AttendanceForbiddenException("User does not have attendance administration permission");
    }

    private boolean hasRole(final AuthenticatedAttendanceUser actor, final String role) {
        return actor.roles().contains(role) || actor.roles().contains("ROLE_" + role);
    }
}

