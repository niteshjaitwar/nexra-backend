package com.nexra.hrms.nexra.modules.crm.service;

import com.nexra.hrms.nexra.modules.crm.config.CrmProperties;
import com.nexra.hrms.nexra.modules.crm.model.CrmLeadStatus;
import com.nexra.hrms.nexra.modules.crm.repository.CrmDealRepository;
import com.nexra.hrms.nexra.modules.crm.repository.CrmLeadRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CrmPipelineMetricsService {

    private final CrmLeadRepository crmLeadRepository;
    private final CrmDealRepository crmDealRepository;
    private final CrmProperties crmProperties;

    @Transactional(readOnly = true)
    public Map<String, Object> snapshot(final String tenantCode, final String moduleKey) {
        final List<String> closedStages = crmProperties.getClosedDealStagesNormalized();
        final CrmLeadStatus wonLeadStatus = CrmLeadStatus.valueOf(crmProperties.getWonLeadStatusNormalized());
        final long totalLeads = crmLeadRepository.countByTenantCodeIgnoreCase(tenantCode);
        final long wonLeadCount = crmLeadRepository.countByTenantCodeIgnoreCaseAndStatus(tenantCode, wonLeadStatus);
        final long totalDeals = crmDealRepository.countByTenantCodeIgnoreCase(tenantCode);
        final long wonDeals = crmDealRepository.countByTenantCodeIgnoreCaseAndStageIgnoreCase(
            tenantCode,
            crmProperties.getWonDealStageNormalized()
        );
        final long openDeals = Math.max(0L, totalDeals - wonDeals);
        final BigDecimal openPipelineValue = closedStages.isEmpty()
            ? BigDecimal.ZERO
            : crmDealRepository.sumOpenPipelineValueByTenantCode(tenantCode, closedStages);

        return Map.of(
            "moduleKey", moduleKey,
            "totalLeads", totalLeads,
            "wonLeadCount", wonLeadCount,
            "totalDeals", totalDeals,
            "wonDeals", wonDeals,
            "openDeals", openDeals,
            "openPipelineValue", openPipelineValue,
            "closedDealStages", closedStages
        );
    }
}
