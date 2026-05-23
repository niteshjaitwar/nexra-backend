package com.nexra.hrms.nexra.modules.hrms.leave.controller;

import com.nexra.hrms.nexra.common.api.ApiResponse;
import com.nexra.hrms.nexra.modules.hrms.leave.dto.request.HolidayUpsertRequest;
import com.nexra.hrms.nexra.modules.hrms.leave.dto.request.LeaveBalanceAdjustRequest;
import com.nexra.hrms.nexra.modules.hrms.leave.dto.request.LeaveDecisionRequest;
import com.nexra.hrms.nexra.modules.hrms.leave.dto.request.LeaveRequestCreateRequest;
import com.nexra.hrms.nexra.modules.hrms.leave.dto.request.LeaveTypeUpsertRequest;
import com.nexra.hrms.nexra.modules.hrms.leave.exception.LeaveForbiddenException;
import com.nexra.hrms.nexra.modules.hrms.leave.exception.LeaveUnauthorizedException;
import com.nexra.hrms.nexra.modules.hrms.leave.model.Holiday;
import com.nexra.hrms.nexra.modules.hrms.leave.model.LeaveBalance;
import com.nexra.hrms.nexra.modules.hrms.leave.model.LeaveRequestView;
import com.nexra.hrms.nexra.modules.hrms.leave.model.LeaveType;
import com.nexra.hrms.nexra.modules.hrms.leave.security.AuthenticatedLeaveUser;
import com.nexra.hrms.nexra.modules.hrms.leave.security.LeaveAuthFilter;
import com.nexra.hrms.nexra.modules.hrms.leave.service.LeaveManagementService;
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
 * Exposes tenant-scoped leave and holiday management APIs.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@Tag(name = "Leave", description = "Leave management APIs.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/leave")
@Slf4j
@Validated
public class LeaveManagementController {

    private final LeaveManagementService leaveManagementService;

    @Operation(summary = "GET /api/v1/leave/status", description = "Processes GET requests for /api/v1/leave/status.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> status() {
        return ResponseEntity.ok(ApiResponse.success("leave service is available.", Map.of(
            "service", "leave",
            "timestamp", Instant.now().toString(),
            "state", "UP"
        )));
    }

    @Operation(summary = "GET /api/v1/leave/capabilities", description = "Processes GET requests for /api/v1/leave/capabilities.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
    @GetMapping("/capabilities")
    public ResponseEntity<ApiResponse<Map<String, Object>>> capabilities() {
        return ResponseEntity.ok(ApiResponse.success("leave capabilities fetched successfully.", Map.of(
            "domains", List.of("leave-types", "holidays", "balances", "leave-requests"),
            "storage", "MySQL + Flyway",
            "auth", "JWT tenant-scoped"
        )));
    }

    @Operation(summary = "PUT /api/v1/leave/leave-types", description = "Processes PUT requests for /api/v1/leave/leave-types.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
    @PutMapping("/leave-types")
    public ResponseEntity<ApiResponse<LeaveType>> upsertLeaveType(
        @Valid @RequestBody final LeaveTypeUpsertRequest request,
        final HttpServletRequest httpRequest
    ) {
        AuthenticatedLeaveUser actor = currentUser(httpRequest);
        requireAdmin(actor);
        log.info("LeaveManagementController - upsertLeaveType - tenantCode={}, code={}", request.tenantCode(), request.code());
        return ResponseEntity.ok(ApiResponse.success("Leave type saved successfully.", leaveManagementService.upsertLeaveType(request, actor)));
    }

    @Operation(summary = "GET /api/v1/leave/leave-types", description = "Processes GET requests for /api/v1/leave/leave-types.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
    @GetMapping("/leave-types")
    public ResponseEntity<ApiResponse<List<LeaveType>>> listLeaveTypes(
        @RequestParam @NotBlank @TenantCode @Size(max = 60) final String tenantCode,
        @RequestParam(defaultValue = "false") final boolean includeInactive,
        final HttpServletRequest httpRequest
    ) {
        log.debug("LeaveManagementController - listLeaveTypes - tenantCode={}, includeInactive={}", tenantCode, includeInactive);
        return ResponseEntity.ok(ApiResponse.success(
            "Leave types fetched successfully.",
            leaveManagementService.listLeaveTypes(tenantCode, includeInactive, currentUser(httpRequest))
        ));
    }

    @Operation(summary = "POST /api/v1/leave/holidays", description = "Processes POST requests for /api/v1/leave/holidays.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
    @PostMapping("/holidays")
    public ResponseEntity<ApiResponse<Holiday>> createHoliday(
        @Valid @RequestBody final HolidayUpsertRequest request,
        final HttpServletRequest httpRequest
    ) {
        return upsertHoliday(request, httpRequest);
    }

    @Operation(summary = "PUT /api/v1/leave/holidays", description = "Processes PUT requests for /api/v1/leave/holidays.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
    @PutMapping("/holidays")
    public ResponseEntity<ApiResponse<Holiday>> upsertHoliday(
        @Valid @RequestBody final HolidayUpsertRequest request,
        final HttpServletRequest httpRequest
    ) {
        AuthenticatedLeaveUser actor = currentUser(httpRequest);
        requireAdmin(actor);
        log.info("LeaveManagementController - upsertHoliday - tenantCode={}, holidayDate={}", request.tenantCode(), request.holidayDate());
        return ResponseEntity.ok(ApiResponse.success("Holiday saved successfully.", leaveManagementService.upsertHoliday(request, actor)));
    }

    @Operation(summary = "GET /api/v1/leave/holidays", description = "Processes GET requests for /api/v1/leave/holidays.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
    @GetMapping("/holidays")
    public ResponseEntity<ApiResponse<List<Holiday>>> listHolidays(
        @RequestParam @NotBlank @TenantCode @Size(max = 60) final String tenantCode,
        @RequestParam(required = false) final LocalDate fromDate,
        @RequestParam(required = false) final LocalDate toDate,
        final HttpServletRequest httpRequest
    ) {
        return ResponseEntity.ok(ApiResponse.success(
            "Holidays fetched successfully.",
            leaveManagementService.listHolidays(tenantCode, fromDate, toDate, currentUser(httpRequest))
        ));
    }

    @Operation(summary = "PUT /api/v1/leave/balances", description = "Processes PUT requests for /api/v1/leave/balances.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
    @PutMapping("/balances")
    public ResponseEntity<ApiResponse<LeaveBalance>> adjustBalance(
        @Valid @RequestBody final LeaveBalanceAdjustRequest request,
        final HttpServletRequest httpRequest
    ) {
        AuthenticatedLeaveUser actor = currentUser(httpRequest);
        requireAdmin(actor);
        log.info("LeaveManagementController - adjustBalance - tenantCode={}, employeeId={}, leaveTypeCode={}",
            request.tenantCode(), request.employeeId(), request.leaveTypeCode());
        return ResponseEntity.ok(ApiResponse.success("Leave balance adjusted successfully.", leaveManagementService.adjustBalance(request, actor)));
    }

    @Operation(summary = "GET /api/v1/leave/balances", description = "Processes GET requests for /api/v1/leave/balances.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
    @GetMapping("/balances")
    public ResponseEntity<ApiResponse<List<LeaveBalance>>> listBalances(
        @RequestParam @NotBlank @TenantCode @Size(max = 60) final String tenantCode,
        @RequestParam final String employeeId,
        final HttpServletRequest httpRequest
    ) {
        return ResponseEntity.ok(ApiResponse.success(
            "Leave balances fetched successfully.",
            leaveManagementService.listBalances(tenantCode, employeeId, currentUser(httpRequest))
        ));
    }

    @Operation(summary = "POST /api/v1/leave/requests", description = "Processes POST requests for /api/v1/leave/requests.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
    @PostMapping("/requests")
    public ResponseEntity<ApiResponse<LeaveRequestView>> createLeaveRequest(
        @Valid @RequestBody final LeaveRequestCreateRequest request,
        final HttpServletRequest httpRequest
    ) {
        log.info("LeaveManagementController - createLeaveRequest - tenantCode={}, employeeId={}, leaveTypeCode={}",
            request.tenantCode(), request.employeeId(), request.leaveTypeCode());
        return ResponseEntity.ok(ApiResponse.success(
            "Leave request created successfully.",
            leaveManagementService.createLeaveRequest(request, currentUser(httpRequest))
        ));
    }

    @Operation(summary = "GET /api/v1/leave/requests", description = "Processes GET requests for /api/v1/leave/requests.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
    @GetMapping("/requests")
    public ResponseEntity<ApiResponse<com.nexra.hrms.nexra.common.api.PageResponse<LeaveRequestView>>> listLeaveRequests(
        @RequestParam @NotBlank @TenantCode @Size(max = 60) final String tenantCode,
        @RequestParam(required = false) final String employeeId,
        @RequestParam(required = false) final String status,
        @RequestParam(defaultValue = "0") @Min(0) final int page,
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) final int size,
        final HttpServletRequest httpRequest
    ) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(
            page, Math.min(size, 100), org.springframework.data.domain.Sort.by("createdAt").descending()
        );
        return ResponseEntity.ok(ApiResponse.success(
            "Leave requests fetched successfully.",
            leaveManagementService.listLeaveRequests(tenantCode, employeeId, status, currentUser(httpRequest), pageable)
        ));
    }

    @Operation(summary = "GET /api/v1/leave/requests/{requestId}", description = "Processes GET requests for /api/v1/leave/requests/{requestId}.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
    @GetMapping("/requests/{requestId}")
    public ResponseEntity<ApiResponse<LeaveRequestView>> getLeaveRequest(
        @PathVariable final String requestId,
        @RequestParam @NotBlank @TenantCode @Size(max = 60) final String tenantCode,
        final HttpServletRequest httpRequest
    ) {
        return ResponseEntity.ok(ApiResponse.success(
            "Leave request fetched successfully.",
            leaveManagementService.getLeaveRequest(tenantCode, requestId, currentUser(httpRequest))
        ));
    }

    @Operation(summary = "POST /api/v1/leave/requests/{requestId}/approve", description = "Processes POST requests for /api/v1/leave/requests/{requestId}/approve.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
    @PostMapping("/requests/{requestId}/approve")
    public ResponseEntity<ApiResponse<LeaveRequestView>> approveLeaveRequest(
        @PathVariable final String requestId,
        @Valid @RequestBody final LeaveDecisionRequest request,
        final HttpServletRequest httpRequest
    ) {
        log.info("LeaveManagementController - approveLeaveRequest - requestId={}, tenantCode={}", requestId, request.tenantCode());
        return ResponseEntity.ok(ApiResponse.success(
            "Leave request approved successfully.",
            leaveManagementService.approveLeaveRequest(requestId, request, currentUser(httpRequest))
        ));
    }

    @Operation(summary = "POST /api/v1/leave/requests/{requestId}/reject", description = "Processes POST requests for /api/v1/leave/requests/{requestId}/reject.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Request processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required or invalid token"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient privileges for this operation"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requested resource not found")
    })
    @PostMapping("/requests/{requestId}/reject")
    public ResponseEntity<ApiResponse<LeaveRequestView>> rejectLeaveRequest(
        @PathVariable final String requestId,
        @Valid @RequestBody final LeaveDecisionRequest request,
        final HttpServletRequest httpRequest
    ) {
        log.info("LeaveManagementController - rejectLeaveRequest - requestId={}, tenantCode={}", requestId, request.tenantCode());
        return ResponseEntity.ok(ApiResponse.success(
            "Leave request rejected successfully.",
            leaveManagementService.rejectLeaveRequest(requestId, request, currentUser(httpRequest))
        ));
    }

    private AuthenticatedLeaveUser currentUser(final HttpServletRequest request) {
        Object value = request.getAttribute(LeaveAuthFilter.ATTR_AUTH_USER);
        if (value instanceof AuthenticatedLeaveUser user) {
            return user;
        }
        throw new LeaveUnauthorizedException("Missing authenticated leave user");
    }

    private void requireAdmin(final AuthenticatedLeaveUser actor) {
        if (hasRole(actor, "PLATFORM_ADMIN") || hasRole(actor, "TENANT_ADMIN") || hasRole(actor, "HR_ADMIN")) {
            return;
        }
        throw new LeaveForbiddenException("User does not have leave administration permission");
    }

    private boolean hasRole(final AuthenticatedLeaveUser actor, final String role) {
        return actor.roles().contains(role) || actor.roles().contains("ROLE_" + role);
    }
}

