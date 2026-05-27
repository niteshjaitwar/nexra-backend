package com.nexra.hrms.nexra.modules.crm.service;

import com.nexra.hrms.nexra.common.api.PageResponse;
import com.nexra.hrms.nexra.modules.crm.dto.request.CrmAccountCreateRequest;
import com.nexra.hrms.nexra.modules.crm.dto.request.CrmAccountUpdateRequest;
import com.nexra.hrms.nexra.modules.crm.model.CrmAccount;
import com.nexra.hrms.nexra.modules.crm.support.CrmAccessScope;

public interface CrmAccountService {

    CrmAccount create(String tenantCode, CrmAccountCreateRequest request, CrmAccessScope accessScope);

    CrmAccount update(String tenantCode, String accountId, CrmAccountUpdateRequest request, CrmAccessScope accessScope);

    CrmAccount findById(String tenantCode, String accountId, CrmAccessScope accessScope);

    PageResponse<CrmAccount> list(String tenantCode, int page, int size, CrmAccessScope accessScope);

    void delete(String tenantCode, String accountId, CrmAccessScope accessScope);
}
