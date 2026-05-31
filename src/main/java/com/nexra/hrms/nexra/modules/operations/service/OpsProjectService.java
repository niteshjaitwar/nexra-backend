package com.nexra.hrms.nexra.modules.operations.service;

import com.nexra.hrms.nexra.common.api.PageResponse;
import com.nexra.hrms.nexra.modules.operations.dto.OpsProjectCreateRequest;
import com.nexra.hrms.nexra.modules.operations.model.OpsProject;

public interface OpsProjectService {

    OpsProject create(String tenantCode, OpsProjectCreateRequest request);

    OpsProject findById(String tenantCode, String projectId);

    PageResponse<OpsProject> list(String tenantCode, int page, int size);

    OpsProject createFromCrmDeal(String tenantCode, String dealId, String ownerUserId);

    OpsProject createFromCrmDealIfAbsent(String tenantCode, String dealId, String ownerUserId);
}
