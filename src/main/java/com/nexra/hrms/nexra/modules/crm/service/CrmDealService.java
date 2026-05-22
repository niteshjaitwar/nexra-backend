package com.nexra.hrms.nexra.modules.crm.service;

import com.nexra.hrms.nexra.common.api.PageResponse;
import com.nexra.hrms.nexra.modules.crm.dto.request.CrmDealCreateRequest;
import com.nexra.hrms.nexra.modules.crm.dto.request.CrmDealUpdateRequest;
import com.nexra.hrms.nexra.modules.crm.model.CrmDeal;

public interface CrmDealService {

    CrmDeal create(String tenantCode, CrmDealCreateRequest request);

    CrmDeal update(String tenantCode, String dealId, CrmDealUpdateRequest request);

    CrmDeal findById(String tenantCode, String dealId);

    PageResponse<CrmDeal> list(String tenantCode, int page, int size);

    void delete(String tenantCode, String dealId);
}

