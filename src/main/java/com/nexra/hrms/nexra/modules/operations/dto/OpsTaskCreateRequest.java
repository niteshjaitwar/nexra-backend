package com.nexra.hrms.nexra.modules.operations.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record OpsTaskCreateRequest(
    @NotBlank @Size(max = 36) String projectId,
    @Size(max = 36) String parentTaskId,
    @NotBlank @Size(max = 240) String title,
    @Size(max = 4000) String description,
    @Size(max = 36) String assigneeUserId,
    @Size(max = 20) String priority,
    LocalDate dueDate,
    BigDecimal estimateHours
) {
}
