package com.nexra.hrms.nexra.modules.crm.service.impl;

import com.nexra.hrms.nexra.common.audit.AuditEventRecord;
import com.nexra.hrms.nexra.common.audit.AuditEventService;
import com.nexra.hrms.nexra.common.exception.NexraValidationException;
import com.nexra.hrms.nexra.modules.crm.dto.request.CrmCustomFieldDefinitionRequest;
import com.nexra.hrms.nexra.modules.crm.dto.request.CrmRecordSharingRuleRequest;
import com.nexra.hrms.nexra.modules.crm.dto.request.CrmWorkflowRuleRequest;
import com.nexra.hrms.nexra.modules.crm.dto.request.IntegrationWebhookSubscriptionRequest;
import com.nexra.hrms.nexra.modules.crm.entity.CrmCustomFieldDefinitionEntity;
import com.nexra.hrms.nexra.modules.crm.entity.CrmRecordSharingRuleEntity;
import com.nexra.hrms.nexra.modules.crm.entity.CrmWorkflowRuleEntity;
import com.nexra.hrms.nexra.modules.crm.entity.IntegrationWebhookSubscriptionEntity;
import com.nexra.hrms.nexra.modules.crm.model.CrmCustomFieldDefinition;
import com.nexra.hrms.nexra.modules.crm.model.CrmRecordSharingRule;
import com.nexra.hrms.nexra.modules.crm.model.CrmWorkflowRule;
import com.nexra.hrms.nexra.modules.crm.model.IntegrationWebhookSubscription;
import com.nexra.hrms.nexra.modules.crm.repository.CrmCustomFieldDefinitionRepository;
import com.nexra.hrms.nexra.modules.crm.repository.CrmRecordSharingRuleRepository;
import com.nexra.hrms.nexra.modules.crm.repository.CrmWorkflowRuleRepository;
import com.nexra.hrms.nexra.modules.crm.repository.IntegrationWebhookSubscriptionRepository;
import com.nexra.hrms.nexra.modules.crm.service.CrmAdministrationService;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CrmAdministrationServiceImpl implements CrmAdministrationService {

    private static final Set<String> MODULE_KEYS = Set.of("crm-leads", "crm-accounts", "crm-contacts", "crm-deals", "crm-activities");
    private static final Map<String, String> MODULE_KEY_ALIASES = Map.ofEntries(
        Map.entry("lead", "crm-leads"),
        Map.entry("leads", "crm-leads"),
        Map.entry("crm-lead", "crm-leads"),
        Map.entry("account", "crm-accounts"),
        Map.entry("accounts", "crm-accounts"),
        Map.entry("crm-account", "crm-accounts"),
        Map.entry("contact", "crm-contacts"),
        Map.entry("contacts", "crm-contacts"),
        Map.entry("crm-contact", "crm-contacts"),
        Map.entry("deal", "crm-deals"),
        Map.entry("deals", "crm-deals"),
        Map.entry("opportunity", "crm-deals"),
        Map.entry("opportunities", "crm-deals"),
        Map.entry("crm-deal", "crm-deals"),
        Map.entry("crm-opportunity", "crm-deals"),
        Map.entry("activity", "crm-activities"),
        Map.entry("activities", "crm-activities"),
        Map.entry("task", "crm-activities"),
        Map.entry("tasks", "crm-activities"),
        Map.entry("crm-activity", "crm-activities")
    );
    private static final Set<String> FIELD_TYPES = Set.of("TEXT", "TEXTAREA", "NUMBER", "DATE", "DATETIME", "BOOLEAN", "PICKLIST", "URL", "EMAIL", "PHONE");
    private static final Set<String> TRIGGER_EVENTS = Set.of("RECORD_CREATED", "RECORD_UPDATED", "RECORD_DELETED", "DATE_REACHED");
    private static final Set<String> PRINCIPAL_TYPES = Set.of("ROLE", "GROUP", "USER", "OWNER_MANAGER");
    private static final Set<String> ACCESS_LEVELS = Set.of("READ", "EDIT");

    private final CrmCustomFieldDefinitionRepository customFieldRepository;
    private final CrmWorkflowRuleRepository workflowRuleRepository;
    private final CrmRecordSharingRuleRepository recordSharingRuleRepository;
    private final IntegrationWebhookSubscriptionRepository webhookRepository;
    private final AuditEventService auditEventService;

    @Override
    public CrmCustomFieldDefinition createCustomField(
        final String tenantCode,
        final String actorEmail,
        final String actorUserId,
        final CrmCustomFieldDefinitionRequest request
    ) {
        final String tenant = normalizeTenant(tenantCode);
        final String moduleKey = normalizeModuleKey(request.moduleKey());
        final String fieldType = normalizeAllowed(request.fieldType(), FIELD_TYPES, "Unsupported CRM custom field type.");
        final String fieldKey = normalize(request.fieldKey()).toLowerCase(Locale.ROOT);
        if (customFieldRepository.existsByTenantCodeIgnoreCaseAndModuleKeyIgnoreCaseAndFieldKeyIgnoreCase(tenant, moduleKey, fieldKey)) {
            throw new NexraValidationException("CRM custom field already exists for this module.");
        }

        final CrmCustomFieldDefinitionEntity entity = new CrmCustomFieldDefinitionEntity();
        entity.setId(UUID.randomUUID().toString());
        entity.setTenantCode(tenant);
        entity.setModuleKey(moduleKey);
        entity.setFieldKey(fieldKey);
        entity.setLabel(normalize(request.label()));
        entity.setFieldType(fieldType);
        entity.setRequired(request.required());
        entity.setSearchable(request.searchable());
        entity.setOptionsJson(normalizeNullable(request.optionsJson()));
        entity.setValidationJson(normalizeNullable(request.validationJson()));
        entity.setActive(request.active());
        final CrmCustomFieldDefinitionEntity saved = customFieldRepository.save(entity);
        auditEventService.record(AuditEventRecord.of(tenant, "CRM", "CREATE_CUSTOM_FIELD", "SUCCESS")
            .withActor(actorEmail, actorUserId)
            .withTarget("CRM_CUSTOM_FIELD", saved.getId())
            .withDetail("{\"moduleKey\":\"" + moduleKey + "\",\"fieldKey\":\"" + fieldKey + "\"}"));
        return toModel(saved);
    }

    @Override
    public List<CrmCustomFieldDefinition> listCustomFields(final String tenantCode, final String moduleKey) {
        final String tenant = normalizeTenant(tenantCode);
        final String module = normalizeModuleKey(moduleKey);
        return customFieldRepository.findAllByTenantCodeIgnoreCaseAndModuleKeyIgnoreCaseOrderByFieldKeyAsc(tenant, module)
            .stream()
            .map(this::toModel)
            .toList();
    }

    @Override
    public CrmWorkflowRule createWorkflowRule(
        final String tenantCode,
        final String actorEmail,
        final String actorUserId,
        final CrmWorkflowRuleRequest request
    ) {
        final String tenant = normalizeTenant(tenantCode);
        final String moduleKey = normalizeModuleKey(request.moduleKey());
        final String triggerEvent = normalizeAllowed(request.triggerEvent(), TRIGGER_EVENTS, "Unsupported CRM workflow trigger event.");

        final CrmWorkflowRuleEntity entity = new CrmWorkflowRuleEntity();
        entity.setId(UUID.randomUUID().toString());
        entity.setTenantCode(tenant);
        entity.setModuleKey(moduleKey);
        entity.setName(normalize(request.name()));
        entity.setTriggerEvent(triggerEvent);
        entity.setCriteriaJson(requireJsonObject(request.criteriaJson(), "criteriaJson"));
        entity.setActionsJson(requireJsonArray(request.actionsJson(), "actionsJson"));
        entity.setPriority(request.priority());
        entity.setActive(request.active());
        final CrmWorkflowRuleEntity saved = workflowRuleRepository.save(entity);
        auditEventService.record(AuditEventRecord.of(tenant, "CRM", "CREATE_WORKFLOW_RULE", "SUCCESS")
            .withActor(actorEmail, actorUserId)
            .withTarget("CRM_WORKFLOW_RULE", saved.getId())
            .withDetail("{\"moduleKey\":\"" + moduleKey + "\",\"triggerEvent\":\"" + triggerEvent + "\"}"));
        return toModel(saved);
    }

    @Override
    public List<CrmWorkflowRule> listWorkflowRules(final String tenantCode, final String moduleKey) {
        final String tenant = normalizeTenant(tenantCode);
        final String module = normalizeModuleKey(moduleKey);
        return workflowRuleRepository.findAllByTenantCodeIgnoreCaseAndModuleKeyIgnoreCaseOrderByPriorityAscNameAsc(tenant, module)
            .stream()
            .map(this::toModel)
            .toList();
    }

    @Override
    public CrmRecordSharingRule createRecordSharingRule(
        final String tenantCode,
        final String actorEmail,
        final String actorUserId,
        final CrmRecordSharingRuleRequest request
    ) {
        final String tenant = normalizeTenant(tenantCode);
        final String moduleKey = normalizeModuleKey(request.moduleKey());
        final String principalType = normalizeAllowed(request.principalType(), PRINCIPAL_TYPES, "Unsupported CRM sharing principal type.");
        final String accessLevel = normalizeAllowed(request.accessLevel(), ACCESS_LEVELS, "Unsupported CRM sharing access level.");

        final CrmRecordSharingRuleEntity entity = new CrmRecordSharingRuleEntity();
        entity.setId(UUID.randomUUID().toString());
        entity.setTenantCode(tenant);
        entity.setModuleKey(moduleKey);
        entity.setName(normalize(request.name()));
        entity.setCriteriaJson(requireJsonObject(request.criteriaJson(), "criteriaJson"));
        entity.setPrincipalType(principalType);
        entity.setPrincipalKey(normalize(request.principalKey()));
        entity.setAccessLevel(accessLevel);
        entity.setActive(request.active());
        final CrmRecordSharingRuleEntity saved = recordSharingRuleRepository.save(entity);
        auditEventService.record(AuditEventRecord.of(tenant, "CRM", "CREATE_SHARING_RULE", "SUCCESS")
            .withActor(actorEmail, actorUserId)
            .withTarget("CRM_SHARING_RULE", saved.getId())
            .withDetail("{\"moduleKey\":\"" + moduleKey + "\",\"principalType\":\"" + principalType + "\"}"));
        return toModel(saved);
    }

    @Override
    public List<CrmRecordSharingRule> listRecordSharingRules(final String tenantCode, final String moduleKey) {
        final String tenant = normalizeTenant(tenantCode);
        final String module = normalizeModuleKey(moduleKey);
        return recordSharingRuleRepository.findAllByTenantCodeIgnoreCaseAndModuleKeyIgnoreCaseOrderByNameAsc(tenant, module)
            .stream()
            .map(this::toModel)
            .toList();
    }

    @Override
    public IntegrationWebhookSubscription createWebhook(
        final String tenantCode,
        final String actorEmail,
        final String actorUserId,
        final IntegrationWebhookSubscriptionRequest request
    ) {
        final String tenant = normalizeTenant(tenantCode);
        final URI uri = validateWebhookUrl(request.targetUrl());
        final String eventType = normalize(request.eventType()).toUpperCase(Locale.ROOT);
        final IntegrationWebhookSubscriptionEntity entity = new IntegrationWebhookSubscriptionEntity();
        entity.setId(UUID.randomUUID().toString());
        entity.setTenantCode(tenant);
        entity.setProductKey("CRM");
        entity.setEventType(eventType);
        entity.setTargetUrl(uri.toString());
        entity.setSecretHash(sha256Hex(request.secret()));
        entity.setActive(request.active());
        final IntegrationWebhookSubscriptionEntity saved = webhookRepository.save(entity);
        auditEventService.record(AuditEventRecord.of(tenant, "CRM", "CREATE_WEBHOOK_SUBSCRIPTION", "SUCCESS")
            .withActor(actorEmail, actorUserId)
            .withTarget("INTEGRATION_WEBHOOK", saved.getId())
            .withDetail("{\"eventType\":\"" + eventType + "\"}"));
        return toModel(saved);
    }

    @Override
    public List<IntegrationWebhookSubscription> listWebhooks(final String tenantCode) {
        final String tenant = normalizeTenant(tenantCode);
        return webhookRepository.findAllByTenantCodeIgnoreCaseAndProductKeyIgnoreCaseOrderByEventTypeAsc(tenant, "CRM")
            .stream()
            .map(this::toModel)
            .toList();
    }

    private URI validateWebhookUrl(final String targetUrl) {
        try {
            final URI uri = new URI(normalize(targetUrl));
            final String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
            if (!Set.of("http", "https").contains(scheme) || uri.getHost() == null || uri.getUserInfo() != null) {
                throw new NexraValidationException("Webhook targetUrl must be an http or https URL without embedded credentials.");
            }
            final int port = uri.getPort();
            if (port != -1 && port != 80 && port != 443) {
                throw new NexraValidationException("Webhook targetUrl may only use port 80 or 443.");
            }
            return uri;
        } catch (URISyntaxException ex) {
            throw new NexraValidationException("Webhook targetUrl is not a valid URI.");
        }
    }

    private String sha256Hex(final String value) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 digest is unavailable.", ex);
        }
    }

    private String requireJsonObject(final String value, final String fieldName) {
        final String normalized = normalize(value);
        if (!normalized.startsWith("{") || !normalized.endsWith("}")) {
            throw new NexraValidationException(fieldName + " must be a JSON object.");
        }
        return normalized;
    }

    private String requireJsonArray(final String value, final String fieldName) {
        final String normalized = normalize(value);
        if (!normalized.startsWith("[") || !normalized.endsWith("]")) {
            throw new NexraValidationException(fieldName + " must be a JSON array.");
        }
        return normalized;
    }

    private String normalizeModuleKey(final String value) {
        final String normalized = normalize(value).toLowerCase(Locale.ROOT).replace('_', '-');
        final String canonical = MODULE_KEY_ALIASES.getOrDefault(normalized, normalized);
        if (!MODULE_KEYS.contains(canonical)) {
            throw new NexraValidationException("Unsupported CRM module key.");
        }
        return canonical;
    }

    private String normalizeAllowed(final String value, final Set<String> allowed, final String errorMessage) {
        final String normalized = normalize(value).toUpperCase(Locale.ROOT);
        if (!allowed.contains(normalized)) {
            throw new NexraValidationException(errorMessage);
        }
        return normalized;
    }

    private String normalizeTenant(final String value) {
        final String normalized = normalize(value);
        if (!normalized.matches("^[A-Za-z0-9_-]{2,60}$")) {
            throw new NexraValidationException("Tenant code must contain only letters, numbers, hyphen, or underscore.");
        }
        return normalized;
    }

    private String normalize(final String value) {
        if (value == null || value.isBlank()) {
            throw new NexraValidationException("Required field is missing.");
        }
        return value.trim();
    }

    private String normalizeNullable(final String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private CrmCustomFieldDefinition toModel(final CrmCustomFieldDefinitionEntity entity) {
        return new CrmCustomFieldDefinition(
            entity.getId(), entity.getTenantCode(), entity.getModuleKey(), entity.getFieldKey(), entity.getLabel(),
            entity.getFieldType(), entity.isRequired(), entity.isSearchable(), entity.getOptionsJson(),
            entity.getValidationJson(), entity.isActive(), entity.getCreatedAt(), entity.getUpdatedAt()
        );
    }

    private CrmWorkflowRule toModel(final CrmWorkflowRuleEntity entity) {
        return new CrmWorkflowRule(
            entity.getId(), entity.getTenantCode(), entity.getModuleKey(), entity.getName(), entity.getTriggerEvent(),
            entity.getCriteriaJson(), entity.getActionsJson(), entity.getPriority(), entity.isActive(),
            entity.getCreatedAt(), entity.getUpdatedAt()
        );
    }

    private CrmRecordSharingRule toModel(final CrmRecordSharingRuleEntity entity) {
        return new CrmRecordSharingRule(
            entity.getId(), entity.getTenantCode(), entity.getModuleKey(), entity.getName(), entity.getCriteriaJson(),
            entity.getPrincipalType(), entity.getPrincipalKey(), entity.getAccessLevel(), entity.isActive(),
            entity.getCreatedAt(), entity.getUpdatedAt()
        );
    }

    private IntegrationWebhookSubscription toModel(final IntegrationWebhookSubscriptionEntity entity) {
        return new IntegrationWebhookSubscription(
            entity.getId(), entity.getTenantCode(), entity.getProductKey(), entity.getEventType(), entity.getTargetUrl(),
            entity.isActive(), entity.getFailureCount(), entity.getLastSuccessAt(), entity.getLastFailureAt(),
            entity.getCreatedAt(), entity.getUpdatedAt()
        );
    }
}
