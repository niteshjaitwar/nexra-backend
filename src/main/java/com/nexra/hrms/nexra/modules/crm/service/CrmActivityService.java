package com.nexra.hrms.nexra.modules.crm.service;

import com.nexra.hrms.nexra.common.api.PageResponse;
import com.nexra.hrms.nexra.modules.crm.dto.request.CrmActivityCreateRequest;
import com.nexra.hrms.nexra.modules.crm.model.CrmActivity;
import com.nexra.hrms.nexra.modules.crm.support.CrmAccessScope;

public interface CrmActivityService {

    CrmActivity create(String tenantCode, CrmActivityCreateRequest request, CrmAccessScope accessScope);

    PageResponse<CrmActivity> list(
        String tenantCode,
        String leadId,
        String contactId,
        String dealId,
        int page,
        int size,
        CrmAccessScope accessScope
    );
}
