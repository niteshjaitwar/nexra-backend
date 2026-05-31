package com.nexra.hrms.nexra.modules.crm.support;

import com.nexra.hrms.nexra.common.exception.NexraValidationException;
import com.nexra.hrms.nexra.modules.crm.entity.CrmCustomFieldDefinitionEntity;
import com.nexra.hrms.nexra.modules.crm.entity.CrmCustomFieldValueEntity;
import com.nexra.hrms.nexra.modules.crm.repository.CrmCustomFieldDefinitionRepository;
import com.nexra.hrms.nexra.modules.crm.repository.CrmCustomFieldValueRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CrmCustomFieldSupport {

    public static final String MODULE_CRM_CONTACTS = "crm-contacts";

    private final CrmCustomFieldDefinitionRepository definitionRepository;
    private final CrmCustomFieldValueRepository valueRepository;

    public Map<String, Object> readValues(final String tenantCode, final String moduleKey, final String recordId) {
        final List<CrmCustomFieldValueEntity> values = valueRepository.findAllByTenantCodeIgnoreCaseAndModuleKeyIgnoreCaseAndRecordId(
            normalize(tenantCode),
            normalize(moduleKey),
            normalize(recordId)
        );
        final Map<String, Object> result = new HashMap<>();
        for (final CrmCustomFieldValueEntity value : values) {
            result.put(value.getFieldKey(), resolveStoredValue(value));
        }
        return Map.copyOf(result);
    }

    public void upsertValues(
        final String tenantCode,
        final String moduleKey,
        final String recordId,
        final Map<String, Object> customFields
    ) {
        if (customFields == null || customFields.isEmpty()) {
            return;
        }
        final String normalizedTenant = normalize(tenantCode);
        final String normalizedModule = normalize(moduleKey);
        final String normalizedRecord = normalize(recordId);
        final List<CrmCustomFieldDefinitionEntity> definitions = definitionRepository
            .findAllByTenantCodeIgnoreCaseAndModuleKeyIgnoreCaseOrderByFieldKeyAsc(normalizedTenant, normalizedModule);
        for (final Map.Entry<String, Object> entry : customFields.entrySet()) {
            final String fieldKey = normalize(entry.getKey());
            final CrmCustomFieldDefinitionEntity definition = definitions.stream()
                .filter((row) -> row.getFieldKey().equalsIgnoreCase(fieldKey) && row.isActive())
                .findFirst()
                .orElseThrow(() -> new NexraValidationException("Unknown or inactive custom field: " + fieldKey));
            final CrmCustomFieldValueEntity entity = valueRepository
                .findByTenantCodeIgnoreCaseAndModuleKeyIgnoreCaseAndRecordIdAndFieldKeyIgnoreCase(
                    normalizedTenant,
                    normalizedModule,
                    normalizedRecord,
                    fieldKey
                )
                .orElseGet(() -> {
                    final CrmCustomFieldValueEntity created = new CrmCustomFieldValueEntity();
                    created.setId(UUID.randomUUID().toString());
                    created.setTenantCode(normalizedTenant);
                    created.setModuleKey(normalizedModule);
                    created.setRecordId(normalizedRecord);
                    created.setFieldKey(fieldKey);
                    return created;
                });
            applyValue(entity, definition.getFieldType(), entry.getValue());
            valueRepository.save(entity);
        }
    }

    private Object resolveStoredValue(final CrmCustomFieldValueEntity entity) {
        if (entity.getValueBoolean() != null) {
            return entity.getValueBoolean();
        }
        if (entity.getValueNumber() != null) {
            return entity.getValueNumber();
        }
        if (entity.getValueDate() != null) {
            return entity.getValueDate().toString();
        }
        if (entity.getValueDatetime() != null) {
            return entity.getValueDatetime().toString();
        }
        return entity.getValueText();
    }

    private void applyValue(final CrmCustomFieldValueEntity entity, final String fieldType, final Object raw) {
        clearValues(entity);
        if (raw == null) {
            return;
        }
        final String normalizedType = fieldType == null ? "TEXT" : fieldType.trim().toUpperCase();
        switch (normalizedType) {
            case "NUMBER", "DECIMAL", "CURRENCY" -> entity.setValueNumber(new BigDecimal(raw.toString()));
            case "BOOLEAN", "CHECKBOX" -> entity.setValueBoolean(Boolean.parseBoolean(raw.toString()));
            case "DATE" -> entity.setValueDate(LocalDate.parse(raw.toString()));
            case "DATETIME" -> entity.setValueDatetime(Instant.parse(raw.toString()));
            default -> entity.setValueText(raw.toString());
        }
    }

    private void clearValues(final CrmCustomFieldValueEntity entity) {
        entity.setValueText(null);
        entity.setValueNumber(null);
        entity.setValueDate(null);
        entity.setValueDatetime(null);
        entity.setValueBoolean(null);
    }

    private String normalize(final String value) {
        if (value == null || value.isBlank()) {
            throw new NexraValidationException("Required field is missing.");
        }
        return value.trim();
    }
}
