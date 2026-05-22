package com.nexra.hrms.nexra.modules.hrms.controller;

import com.nexra.hrms.nexra.common.api.ApiResponse;
import com.nexra.hrms.nexra.common.exception.NexraUnauthorizedException;
import com.nexra.hrms.nexra.common.exception.NexraValidationException;
import com.nexra.hrms.nexra.modules.auth.security.JwtPrincipal;
import com.nexra.hrms.nexra.modules.hrms.attendance.repository.AttendanceRecordRepository;
import com.nexra.hrms.nexra.modules.hrms.leave.repository.LeaveRequestRepository;
import com.nexra.hrms.nexra.modules.hrms.timesheet.repository.TimesheetEntryRepository;
import com.nexra.hrms.nexra.modules.hrms.employee.repository.EmployeeRepository;
import com.nexra.hrms.nexra.modules.payroll.repository.PayrollSlipRepository;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/v1/hrms")
@Tag(name = "HRMS Product", description = "HRMS product workflow and module-level summary endpoints.")
public class HrmsProductController {

    private static final Set<String> SUPPORTED_MODULE_KEYS = Set.of(
        "dashboard",
        "employee-core",
        "attendance",
        "leave",
        "timesheet",
        "payroll"
    );

    private final EmployeeRepository employeeRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final TimesheetEntryRepository timesheetEntryRepository;
    private final PayrollSlipRepository payrollSlipRepository;

    @GetMapping("/modules/{moduleKey}/summary")
    @Operation(summary = "Get HRMS module summary", description = "Returns tenant-scoped operational summary for a supported HRMS module.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Module summary fetched successfully."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid module key."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required.")
    })
    public ResponseEntity<ApiResponse<Map<String, Object>>> moduleSummary(
        @PathVariable @NotBlank @Size(max = 80) final String moduleKey
    ) {
        final String tenantCode = resolveTenantCode();
        validateModuleKey(moduleKey);
        final long queueCount = computeQueueCount(tenantCode, moduleKey);
        final long pendingApprovals = computePendingApprovals(tenantCode, moduleKey);
        final int throughputPercent = (int) Math.max(35L, Math.min(98L, 100L - (pendingApprovals * 100L / Math.max(1L, queueCount))));

        return ResponseEntity.ok(ApiResponse.ok(Map.of(
            "key", moduleKey,
            "queueCount", queueCount,
            "pendingApprovals", pendingApprovals,
            "throughputPercent", throughputPercent
        ), "HRMS module summary fetched successfully."));
    }

    @PostMapping("/workflow")
    @Operation(summary = "Submit HRMS workflow", description = "Accepts tenant-scoped workflow payload for a supported HRMS module.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Workflow accepted."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid payload or unsupported module key."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required.")
    })
    public ResponseEntity<ApiResponse<Map<String, Object>>> workflow(@RequestBody final HrmsWorkflowRequest request) {
        final String tenantCode = resolveTenantCode();
        validateModuleKey(request.moduleKey());
        return ResponseEntity.ok(ApiResponse.ok(Map.of(
            "accepted", true,
            "tenantCode", tenantCode,
            "moduleKey", request.moduleKey(),
            "receivedAt", Instant.now().toString(),
            "workflowRef", "hrms-" + System.nanoTime()
        ), "HRMS workflow accepted successfully."));
    }

    private long computeQueueCount(final String tenantCode, final String moduleKey) {
        return switch (moduleKey) {
            case "employee-core" -> employeeRepository.countByTenantCodeIgnoreCaseAndActiveTrue(tenantCode);
            case "leave" -> leaveRequestRepository.findByTenantCodeIgnoreCaseOrderByCreatedAtDesc(tenantCode).size();
            case "attendance" -> attendanceRecordRepository
                .findByTenantCodeIgnoreCaseAndWorkDateBetweenOrderByWorkDateAsc(tenantCode, LocalDate.now().minusDays(30), LocalDate.now())
                .size();
            case "timesheet" -> timesheetEntryRepository.findByTenantCodeIgnoreCaseOrderByCreatedAtDesc(tenantCode).size();
            case "payroll" -> payrollSlipRepository.findByTenantCodeIgnoreCaseOrderByGeneratedAtDesc(tenantCode).size();
            default -> employeeRepository.countByTenantCodeIgnoreCaseAndActiveTrue(tenantCode)
                + leaveRequestRepository.findByTenantCodeIgnoreCaseOrderByCreatedAtDesc(tenantCode).size();
        };
    }

    private long computePendingApprovals(final String tenantCode, final String moduleKey) {
        return switch (moduleKey) {
            case "leave" -> leaveRequestRepository.findByTenantCodeIgnoreCaseOrderByCreatedAtDesc(tenantCode).stream()
                .filter((row) -> "SUBMITTED".equalsIgnoreCase(row.getStatus()) || "PENDING".equalsIgnoreCase(row.getStatus()))
                .count();
            case "timesheet" -> timesheetEntryRepository.findByTenantCodeIgnoreCaseOrderByCreatedAtDesc(tenantCode).stream()
                .filter((row) -> "SUBMITTED".equalsIgnoreCase(row.getStatus()) || "PENDING".equalsIgnoreCase(row.getStatus()))
                .count();
            case "attendance" -> attendanceRecordRepository
                .findByTenantCodeIgnoreCaseAndWorkDateBetweenOrderByWorkDateAsc(tenantCode, LocalDate.now().minusDays(30), LocalDate.now())
                .stream()
                .filter((row) -> row.getCheckOutAt() == null)
                .count();
            default -> Math.max(1L, computeQueueCount(tenantCode, moduleKey) / 4L);
        };
    }

    private String resolveTenantCode() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof JwtPrincipal principal) {
            if (!StringUtils.hasText(principal.tenantCode())) {
                throw new NexraUnauthorizedException("Authenticated user is missing tenant context.");
            }
            return principal.tenantCode().trim();
        }
        throw new NexraUnauthorizedException("Authentication is required.");
    }

    private void validateModuleKey(final String moduleKey) {
        if (SUPPORTED_MODULE_KEYS.contains(moduleKey)) {
            return;
        }
        throw new NexraValidationException("Unsupported HRMS module key: " + moduleKey);
    }

    public record HrmsWorkflowRequest(
        @NotBlank @Size(max = 80) String moduleKey,
        @Email @Size(max = 160) String actorEmail,
        Map<String, Object> payload
    ) {
    }
}
