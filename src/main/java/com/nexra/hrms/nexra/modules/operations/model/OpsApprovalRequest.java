package com.nexra.hrms.nexra.modules.operations.model;

public record OpsApprovalRequest(
    String id,
    String referenceType,
    String referenceId,
    String status,
    String requestedByUserId,
    String approverUserId
) {
}
