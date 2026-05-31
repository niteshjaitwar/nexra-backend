package com.nexra.hrms.nexra.modules.crm.service;

import com.nexra.hrms.nexra.common.api.PageResponse;
import com.nexra.hrms.nexra.modules.crm.dto.request.CrmCaseAssignRequest;
import com.nexra.hrms.nexra.modules.crm.dto.request.CrmCaseCreateRequest;
import com.nexra.hrms.nexra.modules.crm.dto.request.CrmCaseStatusUpdateRequest;
import com.nexra.hrms.nexra.modules.crm.model.CrmCase;

/**
 * Business operations for CRM support cases including a configurable status
 * state machine. All mutations are tenant-scoped and audited.
 */
public interface CrmCaseService {

    CrmCase create(String tenantCode, String actorEmail, CrmCaseCreateRequest request);

    CrmCase findById(String tenantCode, String caseId);

    PageResponse<CrmCase> list(String tenantCode, int page, int size);

    CrmCase transitionStatus(String tenantCode, String actorEmail, String caseId, CrmCaseStatusUpdateRequest request);

    CrmCase assign(String tenantCode, String actorEmail, String caseId, CrmCaseAssignRequest request);
}
