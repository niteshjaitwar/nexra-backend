package com.nexra.hrms.nexra.modules.crm.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CrmWorkflowRuleRequest(
    @NotBlank
    @Size(max = 60)
    String moduleKey,
    @NotBlank
    @Size(max = 160)
    String name,
    @NotBlank
    @Size(max = 60)
    String triggerEvent,
    @NotBlank
    @Size(max = 20000)
    String criteriaJson,
    @NotBlank
    @Size(max = 20000)
    String actionsJson,
    @Min(1)
    @Max(10000)
    int priority,
    boolean active
) {
}
