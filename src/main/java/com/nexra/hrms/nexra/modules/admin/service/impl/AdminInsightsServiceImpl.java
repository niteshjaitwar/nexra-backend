package com.nexra.hrms.nexra.modules.admin.service.impl;

import com.nexra.hrms.nexra.common.audit.AuditEventService;
import com.nexra.hrms.nexra.common.audit.AuditEventView;
import com.nexra.hrms.nexra.modules.admin.service.AdminInsightsService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminInsightsServiceImpl implements AdminInsightsService {

    private final JdbcTemplate jdbcTemplate;
    private final AuditEventService auditEventService;

    @Override
    public Map<String, Long> tenantSummary(
        final String tenantCode,
        final Set<String> products,
        final boolean platformAdmin
    ) {
        final Map<String, Long> summary = new LinkedHashMap<>();
        if (platformAdmin || products.contains("HRMS")) {
            summary.put("employees", count("SELECT COUNT(*) FROM ec_employees WHERE tenant_code = ?", tenantCode));
            summary.put("departments", count("SELECT COUNT(*) FROM ec_departments WHERE tenant_code = ?", tenantCode));
            summary.put("attendanceRecords", count("SELECT COUNT(*) FROM at_records WHERE tenant_code = ?", tenantCode));
            summary.put("leaveRequests", count("SELECT COUNT(*) FROM lv_leave_requests WHERE tenant_code = ?", tenantCode));
            summary.put("timesheetEntries", count("SELECT COUNT(*) FROM ts_entries WHERE tenant_code = ?", tenantCode));
            summary.put("expenseClaims", count("SELECT COUNT(*) FROM ex_claims WHERE tenant_code = ?", tenantCode));
        }
        if (platformAdmin || products.contains("PAYROLL")) {
            summary.put("payrollSlips", count("SELECT COUNT(*) FROM payroll_slips WHERE tenant_code = ?", tenantCode));
        }
        if (platformAdmin || products.contains("CRM")) {
            summary.put("crmLeads", count("SELECT COUNT(*) FROM crm_leads WHERE tenant_code = ?", tenantCode));
            summary.put("crmAccounts", count("SELECT COUNT(*) FROM crm_accounts WHERE tenant_code = ?", tenantCode));
            summary.put("crmDeals", count("SELECT COUNT(*) FROM crm_deals WHERE tenant_code = ?", tenantCode));
        }
        return summary;
    }

    @Override
    public List<AuditEventView> recentAuditEvents(final String tenantCode, final String module, final int limit) {
        final int boundedLimit = Math.max(1, Math.min(limit, 200));
        if (module == null || module.isBlank()) {
            return auditEventService.getRecentByTenant(tenantCode, boundedLimit);
        }
        return auditEventService.getByTenantAndModule(tenantCode, module.trim().toUpperCase(), boundedLimit);
    }

    private long count(final String sql, final String tenantCode) {
        final Long value = jdbcTemplate.queryForObject(sql, Long.class, tenantCode);
        return value == null ? 0L : value;
    }
}
