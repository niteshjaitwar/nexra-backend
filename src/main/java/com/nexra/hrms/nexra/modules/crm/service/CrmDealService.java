package com.nexra.hrms.nexra.modules.crm.service;

import com.nexra.hrms.nexra.common.api.PageResponse;
import com.nexra.hrms.nexra.modules.crm.dto.request.CrmDealCreateRequest;
import com.nexra.hrms.nexra.modules.crm.dto.request.CrmDealStageUpdateRequest;
import com.nexra.hrms.nexra.modules.crm.dto.request.CrmDealUpdateRequest;
import com.nexra.hrms.nexra.modules.crm.model.CrmDeal;
import com.nexra.hrms.nexra.modules.crm.support.CrmAccessScope;

public interface CrmDealService {

    CrmDeal create(String tenantCode, CrmDealCreateRequest request, CrmAccessScope accessScope);

    CrmDeal update(String tenantCode, String dealId, CrmDealUpdateRequest request, CrmAccessScope accessScope);

    CrmDeal findById(String tenantCode, String dealId, CrmAccessScope accessScope);

    PageResponse<CrmDeal> list(String tenantCode, int page, int size, CrmAccessScope accessScope);

    void delete(String tenantCode, String dealId, CrmAccessScope accessScope);

    CrmDeal transitionStage(String tenantCode, String actorEmail, String dealId, CrmDealStageUpdateRequest request, CrmAccessScope accessScope);
}
