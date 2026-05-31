package com.nexra.hrms.nexra.modules.operations.service;

import com.nexra.hrms.nexra.common.api.PageResponse;
import com.nexra.hrms.nexra.modules.operations.dto.OpsApprovalCreateRequest;
import com.nexra.hrms.nexra.modules.operations.dto.OpsApprovalDecisionRequest;
import com.nexra.hrms.nexra.modules.operations.model.OpsApprovalRequest;

public interface OpsApprovalService {

    OpsApprovalRequest create(String tenantCode, String actorEmail, OpsApprovalCreateRequest request);

    OpsApprovalRequest decide(
        String tenantCode,
        String actorEmail,
        String actorUserId,
        java.util.Set<String> actorRoles,
        String approvalId,
        OpsApprovalDecisionRequest request
    );

    PageResponse<OpsApprovalRequest> list(String tenantCode, int page, int size);
}
