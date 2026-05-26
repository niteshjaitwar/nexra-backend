package com.nexra.hrms.nexra.modules.crm.service;

import com.nexra.hrms.nexra.common.api.PageResponse;
import com.nexra.hrms.nexra.modules.crm.dto.request.CrmActivityCreateRequest;
import com.nexra.hrms.nexra.modules.crm.model.CrmActivity;

public interface CrmActivityService {

    CrmActivity create(String tenantCode, CrmActivityCreateRequest request);

    PageResponse<CrmActivity> list(
        String tenantCode,
        String leadId,
        String contactId,
        String dealId,
        int page,
        int size
    );
}
