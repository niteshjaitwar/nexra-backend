package com.nexra.hrms.nexra.modules.crm.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public record CrmCaseCreateRequest(
    @NotBlank @Size(max = 240) String subject,
    @Size(max = 4000) String description,
    @Size(max = 20) String priority,
    @Size(max = 36) String accountId,
    @Size(max = 36) String contactId,
    @NotBlank @Size(max = 36) String ownerUserId,
    Instant slaDueAt
) {
}
