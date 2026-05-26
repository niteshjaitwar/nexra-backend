package com.nexra.hrms.nexra.modules.crm.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CrmCustomFieldDefinitionRequest(
    @NotBlank
    @Size(max = 60)
    String moduleKey,
    @NotBlank
    @Pattern(regexp = "^[a-z][a-z0-9_]{1,79}$", message = "fieldKey must be snake_case and start with a letter.")
    String fieldKey,
    @NotBlank
    @Size(max = 120)
    String label,
    @NotBlank
    @Size(max = 40)
    String fieldType,
    boolean required,
    boolean searchable,
    @Size(max = 10000)
    String optionsJson,
    @Size(max = 10000)
    String validationJson,
    boolean active
) {
}
