package com.nexra.hrms.nexra.modules.crm.service;

import com.nexra.hrms.nexra.common.api.PageResponse;
import com.nexra.hrms.nexra.modules.crm.dto.request.CrmQuoteCreateRequest;
import com.nexra.hrms.nexra.modules.crm.dto.request.CrmQuoteStatusUpdateRequest;
import com.nexra.hrms.nexra.modules.crm.model.CrmQuote;

public interface CrmQuoteService {

    CrmQuote create(String tenantCode, String actorEmail, CrmQuoteCreateRequest request);

    CrmQuote findById(String tenantCode, String quoteId);

    PageResponse<CrmQuote> list(String tenantCode, int page, int size);

    CrmQuote transitionStatus(String tenantCode, String actorEmail, String quoteId, CrmQuoteStatusUpdateRequest request);
}
