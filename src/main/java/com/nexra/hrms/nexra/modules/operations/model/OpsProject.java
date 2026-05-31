package com.nexra.hrms.nexra.modules.operations.model;

import java.time.Instant;
import java.time.LocalDate;

public record OpsProject(
    String id,
    String tenantCode,
    String code,
    String name,
    String description,
    String ownerUserId,
    String status,
    String crmDealId,
    String departmentCode,
    LocalDate startDate,
    LocalDate endDate,
    Instant createdAt,
    Instant updatedAt
) {
}
