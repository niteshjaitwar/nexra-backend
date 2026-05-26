package com.nexra.hrms.nexra.modules.crm.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CrmRecordSharingRuleRequest(
    @NotBlank
    @Size(max = 60)
    String moduleKey,
    @NotBlank
    @Size(max = 160)
    String name,
    @NotBlank
    @Size(max = 20000)
    String criteriaJson,
    @NotBlank
    @Size(max = 40)
    String principalType,
    @NotBlank
    @Size(max = 120)
    String principalKey,
    @NotBlank
    @Size(max = 30)
    String accessLevel,
    boolean active
) {
}
