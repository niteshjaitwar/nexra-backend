package com.nexra.hrms.nexra.modules.crm.service;

import com.nexra.hrms.nexra.common.api.PageResponse;
import com.nexra.hrms.nexra.modules.crm.dto.request.CrmCampaignCreateRequest;
import com.nexra.hrms.nexra.modules.crm.dto.request.CrmCampaignStatusUpdateRequest;
import com.nexra.hrms.nexra.modules.crm.dto.request.CrmLeadCreateRequest;
import com.nexra.hrms.nexra.modules.crm.model.CrmCampaign;
import com.nexra.hrms.nexra.modules.crm.model.CrmLead;
import com.nexra.hrms.nexra.modules.crm.support.CrmAccessScope;

public interface CrmCampaignService {

    CrmCampaign create(String tenantCode, String actorEmail, CrmCampaignCreateRequest request);

    CrmCampaign findById(String tenantCode, String campaignId);

    PageResponse<CrmCampaign> list(String tenantCode, int page, int size);

    CrmCampaign transitionStatus(String tenantCode, String actorEmail, String campaignId, CrmCampaignStatusUpdateRequest request);

    /**
     * Registers a lead attributed to an active campaign (closed-loop marketing attribution).
     */
    CrmLead captureLead(
        String tenantCode,
        String campaignId,
        String actorEmail,
        CrmLeadCreateRequest request,
        CrmAccessScope accessScope
    );

    /**
     * Lists leads captured from a campaign.
     */
    PageResponse<CrmLead> listCampaignLeads(
        String tenantCode,
        String campaignId,
        int page,
        int size,
        CrmAccessScope accessScope
    );
}
