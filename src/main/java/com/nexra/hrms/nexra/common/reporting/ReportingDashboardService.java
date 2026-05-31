package com.nexra.hrms.nexra.common.reporting;

import com.nexra.hrms.nexra.modules.crm.service.CrmPipelineMetricsService;
import com.nexra.hrms.nexra.modules.hrms.service.HrmsProductSummaryService;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportingDashboardService {

    private final HrmsProductSummaryService hrmsProductSummaryService;
    private final CrmPipelineMetricsService crmPipelineMetricsService;

    @Transactional(readOnly = true)
    public Map<String, Object> tenantDashboard(final String tenantCode) {
        final HrmsProductSummaryService.ModuleSummaryCounts leaveCounts =
            hrmsProductSummaryService.resolveCounts(tenantCode, "leave");
        final HrmsProductSummaryService.ModuleSummaryCounts employeeCounts =
            hrmsProductSummaryService.resolveCounts(tenantCode, "employee-core");
        final Map<String, Object> crmSnapshot = crmPipelineMetricsService.snapshot(tenantCode, "crm-dashboard");

        final Map<String, Object> dashboard = new LinkedHashMap<>();
        dashboard.put("activeEmployees", employeeCounts.queueCount());
        dashboard.put("pendingLeaveRequests", leaveCounts.pendingApprovals());
        dashboard.put("totalLeads", crmSnapshot.get("totalLeads"));
        dashboard.put("openPipelineValue", crmSnapshot.get("openPipelineValue"));
        return Map.copyOf(dashboard);
    }
}
