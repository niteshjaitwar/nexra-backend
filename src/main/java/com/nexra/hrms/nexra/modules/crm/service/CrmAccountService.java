package com.nexra.hrms.nexra.modules.crm.service;

import com.nexra.hrms.nexra.common.api.PageResponse;
import com.nexra.hrms.nexra.modules.crm.dto.request.CrmAccountCreateRequest;
import com.nexra.hrms.nexra.modules.crm.dto.request.CrmAccountUpdateRequest;
import com.nexra.hrms.nexra.modules.crm.model.CrmAccount;

public interface CrmAccountService {

    CrmAccount create(String tenantCode, CrmAccountCreateRequest request);

    CrmAccount update(String tenantCode, String accountId, CrmAccountUpdateRequest request);

    CrmAccount findById(String tenantCode, String accountId);

    PageResponse<CrmAccount> list(String tenantCode, int page, int size);

    void delete(String tenantCode, String accountId);
}

