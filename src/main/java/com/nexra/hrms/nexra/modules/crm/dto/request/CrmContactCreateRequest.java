package com.nexra.hrms.nexra.modules.crm.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Map;

public record CrmContactCreateRequest(
    @Size(max = 36) String accountId,
    @NotBlank @Size(max = 180) String fullName,
    @Email @Size(max = 180) String email,
    @Size(max = 40) String phone,
    @NotBlank @Size(max = 36) String ownerUserId,
    Map<String, Object> customFields
) {
}
