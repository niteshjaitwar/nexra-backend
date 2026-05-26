package com.nexra.hrms.nexra.modules.crm.model;

import java.time.Instant;

public record CrmCustomFieldDefinition(
    String id,
    String tenantCode,
    String moduleKey,
    String fieldKey,
    String label,
    String fieldType,
    boolean required,
    boolean searchable,
    String optionsJson,
    String validationJson,
    boolean active,
    Instant createdAt,
    Instant updatedAt
) {
}
