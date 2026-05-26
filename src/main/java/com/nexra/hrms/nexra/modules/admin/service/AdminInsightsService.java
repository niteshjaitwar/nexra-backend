package com.nexra.hrms.nexra.modules.admin.service;

import com.nexra.hrms.nexra.common.audit.AuditEventView;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface AdminInsightsService {

    Map<String, Long> tenantSummary(String tenantCode, Set<String> products, boolean platformAdmin);

    List<AuditEventView> recentAuditEvents(String tenantCode, String module, int limit);
}
