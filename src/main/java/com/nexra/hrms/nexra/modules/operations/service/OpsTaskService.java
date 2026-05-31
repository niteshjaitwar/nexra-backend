package com.nexra.hrms.nexra.modules.operations.service;

import com.nexra.hrms.nexra.common.api.PageResponse;
import com.nexra.hrms.nexra.modules.operations.dto.OpsTaskCreateRequest;
import com.nexra.hrms.nexra.modules.operations.dto.OpsTaskStatusUpdateRequest;
import com.nexra.hrms.nexra.modules.operations.model.OpsTask;

public interface OpsTaskService {

    OpsTask create(String tenantCode, String actorEmail, OpsTaskCreateRequest request);

    PageResponse<OpsTask> listByProject(String tenantCode, String projectId, int page, int size);

    OpsTask updateStatus(String tenantCode, String actorEmail, String taskId, OpsTaskStatusUpdateRequest request);
}
