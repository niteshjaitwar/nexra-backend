package com.nexra.hrms.nexra.modules.operations.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OpsTask(
    String id,
    String projectId,
    String title,
    String status,
    String priority,
    String assigneeUserId,
    LocalDate dueDate,
    BigDecimal estimateHours
) {
}
