package com.nexra.hrms.nexra.modules.admin.service;

import com.nexra.hrms.nexra.common.audit.AuditEventView;

import java.util.List;
import java.util.Map;

public interface AdminInsightsService {

    Map<String, Long> tenantSummary(String tenantCode);

    List<AuditEventView> recentAuditEvents(String tenantCode, String module, int limit);
}

