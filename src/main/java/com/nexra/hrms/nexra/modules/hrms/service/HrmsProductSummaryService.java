package com.nexra.hrms.nexra.modules.hrms.service;

import com.nexra.hrms.nexra.modules.hrms.attendance.repository.AttendanceRecordRepository;
import com.nexra.hrms.nexra.modules.hrms.config.HrmsProductSummaryProperties;
import com.nexra.hrms.nexra.modules.hrms.config.HrmsProductSummaryProperties.ModuleSummaryRule;
import com.nexra.hrms.nexra.modules.hrms.config.HrmsProductSummaryProperties.SummaryMetric;
import com.nexra.hrms.nexra.modules.hrms.employee.repository.EmployeeRepository;
import com.nexra.hrms.nexra.modules.hrms.expense.repository.ExpenseClaimRepository;
import com.nexra.hrms.nexra.modules.hrms.leave.repository.LeaveRequestRepository;
import com.nexra.hrms.nexra.modules.hrms.onboarding.repository.OnboardingPlanRepository;
import com.nexra.hrms.nexra.modules.hrms.performance.repository.GoalRepository;
import com.nexra.hrms.nexra.modules.hrms.recruitment.repository.CandidateRepository;
import com.nexra.hrms.nexra.modules.hrms.timesheet.repository.TimesheetEntryRepository;
import com.nexra.hrms.nexra.modules.payroll.repository.PayrollSlipRepository;
import java.time.LocalDate;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HrmsProductSummaryService {

    private final HrmsProductSummaryProperties properties;
    private final EmployeeRepository employeeRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final TimesheetEntryRepository timesheetEntryRepository;
    private final PayrollSlipRepository payrollSlipRepository;
    private final ExpenseClaimRepository expenseClaimRepository;
    private final OnboardingPlanRepository onboardingPlanRepository;
    private final GoalRepository goalRepository;
    private final CandidateRepository candidateRepository;

    @Transactional(readOnly = true)
    public ModuleSummaryCounts resolveCounts(final String tenantCode, final String moduleKey) {
        final ModuleSummaryRule rule = properties.getModules().getOrDefault(moduleKey, new ModuleSummaryRule());
        final long queueCount = resolveMetric(tenantCode, moduleKey, rule.getQueueMetric(), rule);
        final long pendingApprovals = resolveMetric(tenantCode, moduleKey, rule.getPendingMetric(), rule);
        final int throughputPercent = queueCount == 0
            ? 100
            : (int) Math.max(0L, Math.min(100L, ((queueCount - pendingApprovals) * 100L) / queueCount));
        return new ModuleSummaryCounts(queueCount, pendingApprovals, throughputPercent);
    }

    private long resolveMetric(
        final String tenantCode,
        final String moduleKey,
        final SummaryMetric metric,
        final ModuleSummaryRule rule
    ) {
        if (metric == null) {
            return 0L;
        }
        return switch (metric) {
            case ACTIVE_EMPLOYEES -> employeeRepository.countByTenantCodeIgnoreCaseAndActiveTrue(tenantCode);
            case TOTAL_RECORDS -> countTotalRecords(tenantCode, moduleKey);
            case ATTENDANCE_WINDOW -> attendanceRecordRepository.countByTenantCodeIgnoreCaseAndWorkDateBetween(
                tenantCode,
                LocalDate.now().minusDays(properties.getAttendanceLookbackDays()),
                LocalDate.now()
            );
            case STATUS_IN -> leaveOrTimesheetStatusIn(tenantCode, moduleKey, rule.getPendingStatuses());
            case STATUS_EQUALS -> countByStatusEquals(tenantCode, moduleKey, rule.getPendingStatus());
            case STAGE_EQUALS -> candidateRepository.countByTenantCodeAndStage(tenantCode, rule.getPendingStage());
            case OPEN_CHECKOUT_IN_WINDOW -> attendanceRecordRepository.countByTenantCodeIgnoreCaseAndWorkDateBetweenAndCheckOutAtIsNull(
                tenantCode,
                LocalDate.now().minusDays(properties.getAttendanceLookbackDays()),
                LocalDate.now()
            );
            case QUEUE_FRACTION -> Math.max(0L, countTotalRecords(tenantCode, moduleKey) / 4L);
        };
    }

    private long countTotalRecords(final String tenantCode, final String moduleKey) {
        return switch (moduleKey) {
            case "leave" -> leaveRequestRepository.countByTenantCodeIgnoreCase(tenantCode);
            case "timesheet" -> timesheetEntryRepository.countByTenantCodeIgnoreCase(tenantCode);
            case "payroll" -> payrollSlipRepository.countByTenantCodeIgnoreCase(tenantCode);
            case "expense" -> expenseClaimRepository.countByTenantCodeIgnoreCase(tenantCode);
            case "onboarding" -> onboardingPlanRepository.countByTenantCode(tenantCode);
            case "performance" -> goalRepository.countByTenantCode(tenantCode);
            case "recruitment" -> candidateRepository.countByTenantCode(tenantCode);
            default -> employeeRepository.countByTenantCodeIgnoreCaseAndActiveTrue(tenantCode);
        };
    }

    private long leaveOrTimesheetStatusIn(final String tenantCode, final String moduleKey, final java.util.List<String> statuses) {
        return switch (moduleKey) {
            case "leave" -> leaveRequestRepository.countByTenantCodeIgnoreCaseAndStatusIn(tenantCode, statuses);
            case "timesheet" -> timesheetEntryRepository.countByTenantCodeIgnoreCaseAndStatusIn(tenantCode, statuses);
            default -> 0L;
        };
    }

    private long countByStatusEquals(final String tenantCode, final String moduleKey, final String status) {
        if (status == null || status.isBlank()) {
            return 0L;
        }
        return switch (moduleKey) {
            case "expense" -> expenseClaimRepository.countByTenantCodeIgnoreCaseAndStatusIgnoreCase(tenantCode, status);
            case "onboarding" -> onboardingPlanRepository.countByTenantCodeAndStatus(tenantCode, status);
            case "performance" -> goalRepository.countByTenantCodeAndStatus(tenantCode, status);
            default -> 0L;
        };
    }

    public record ModuleSummaryCounts(long queueCount, long pendingApprovals, int throughputPercent) {
    }
}
