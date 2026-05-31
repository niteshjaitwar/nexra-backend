package com.nexra.hrms.nexra.modules.crm.service;

import com.nexra.hrms.nexra.common.api.PageResponse;
import com.nexra.hrms.nexra.modules.crm.dto.request.CrmContactCreateRequest;
import com.nexra.hrms.nexra.modules.crm.dto.request.CrmContactUpdateRequest;
import com.nexra.hrms.nexra.modules.crm.model.CrmContact;
import com.nexra.hrms.nexra.modules.crm.support.CrmAccessScope;

public interface CrmContactService {

    CrmContact create(String tenantCode, CrmContactCreateRequest request, CrmAccessScope accessScope);

    CrmContact update(String tenantCode, String contactId, CrmContactUpdateRequest request, CrmAccessScope accessScope);

    CrmContact findById(String tenantCode, String contactId, CrmAccessScope accessScope);

    PageResponse<CrmContact> list(String tenantCode, int page, int size, CrmAccessScope accessScope);

    void delete(String tenantCode, String contactId, CrmAccessScope accessScope);
}
