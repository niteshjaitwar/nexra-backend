package com.nexra.hrms.nexra.modules.crm.service.impl;

import com.nexra.hrms.nexra.common.audit.AuditEventRecord;
import com.nexra.hrms.nexra.common.audit.AuditEventRepository;
import com.nexra.hrms.nexra.common.audit.AuditEventService;
import com.nexra.hrms.nexra.common.exception.NexraValidationException;
import com.nexra.hrms.nexra.modules.crm.config.CrmProperties;
import com.nexra.hrms.nexra.modules.crm.dto.request.CrmCustomFieldDefinitionRequest;
import com.nexra.hrms.nexra.modules.crm.dto.request.CrmRecordSharingRuleRequest;
import com.nexra.hrms.nexra.modules.crm.dto.request.CrmWorkflowRuleRequest;
import com.nexra.hrms.nexra.modules.crm.dto.request.IntegrationWebhookSignatureVerificationRequest;
import com.nexra.hrms.nexra.modules.crm.dto.request.IntegrationWebhookSubscriptionRequest;
import com.nexra.hrms.nexra.modules.crm.entity.CrmCustomFieldDefinitionEntity;
import com.nexra.hrms.nexra.modules.crm.entity.CrmRecordSharingRuleEntity;
import com.nexra.hrms.nexra.modules.crm.entity.CrmWorkflowRuleEntity;
import com.nexra.hrms.nexra.modules.crm.entity.IntegrationWebhookDeliveryEntity;
import com.nexra.hrms.nexra.modules.crm.entity.IntegrationWebhookDeliveryStatus;
import com.nexra.hrms.nexra.modules.crm.entity.IntegrationWebhookSubscriptionEntity;
import com.nexra.hrms.nexra.modules.crm.model.CrmCustomFieldDefinition;
import com.nexra.hrms.nexra.modules.crm.model.CrmRecordSharingRule;
import com.nexra.hrms.nexra.modules.crm.model.CrmWorkflowRule;
import com.nexra.hrms.nexra.modules.crm.model.IntegrationWebhookDeliveryAlertStatus;
import com.nexra.hrms.nexra.modules.crm.model.IntegrationWebhookDelivery;
import com.nexra.hrms.nexra.modules.crm.model.IntegrationWebhookDeliveryMetrics;
import com.nexra.hrms.nexra.modules.crm.model.IntegrationWebhookReplayAuditView;
import com.nexra.hrms.nexra.modules.crm.model.IntegrationWebhookSignatureVerification;
import com.nexra.hrms.nexra.modules.crm.model.IntegrationWebhookSubscription;
import com.nexra.hrms.nexra.modules.crm.repository.CrmCustomFieldDefinitionRepository;
import com.nexra.hrms.nexra.modules.crm.repository.CrmRecordSharingRuleRepository;
import com.nexra.hrms.nexra.modules.crm.repository.CrmWorkflowRuleRepository;
import com.nexra.hrms.nexra.modules.crm.repository.IntegrationWebhookDeliveryRepository;
import com.nexra.hrms.nexra.modules.crm.repository.IntegrationWebhookSubscriptionRepository;
import com.nexra.hrms.nexra.modules.crm.service.CrmAdministrationService;
import com.nexra.hrms.nexra.modules.crm.support.CrmWebhookReplayGuard;
import com.nexra.hrms.nexra.modules.crm.support.CrmWebhookSignatureCodec;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
    private static final long REPLAY_COOLDOWN_SECONDS = 30;

    private final CrmProperties crmProperties;
    private final CrmCustomFieldDefinitionRepository customFieldRepository;
    private final CrmWorkflowRuleRepository workflowRuleRepository;
    private final CrmRecordSharingRuleRepository recordSharingRuleRepository;
    private final IntegrationWebhookSubscriptionRepository webhookRepository;
    private final IntegrationWebhookDeliveryRepository webhookDeliveryRepository;
    private final CrmWebhookDeliveryService webhookDeliveryService;
    private final CrmWebhookReplayGuard webhookReplayGuard;
    private final AuditEventRepository auditEventRepository;
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
        entity.setSecretHash(CrmWebhookSignatureCodec.secretHash(request.secret()));
        entity.setActive(request.active());
        final IntegrationWebhookSubscriptionEntity saved = webhookRepository.save(entity);
        webhookDeliveryService.enqueueForSubscription(
            saved,
            "{\"type\":\"WEBHOOK_SUBSCRIPTION_CREATED\",\"subscriptionId\":\"" + saved.getId() + "\"}"
        );
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

    @Override
    public List<IntegrationWebhookDelivery> listWebhookDeliveries(final String tenantCode) {
        final String tenant = normalizeTenant(tenantCode);
        return webhookDeliveryRepository.findTop100ByTenantCodeIgnoreCaseOrderByCreatedAtDesc(tenant)
            .stream()
            .map(this::toModel)
            .toList();
    }

    @Override
    public IntegrationWebhookDelivery replayWebhookDelivery(
        final String tenantCode,
        final String actorEmail,
        final String actorUserId,
        final String deliveryId
    ) {
        final String tenant = normalizeTenant(tenantCode);
        final IntegrationWebhookDeliveryEntity delivery = webhookDeliveryRepository.findByIdAndTenantCodeIgnoreCase(
                normalize(deliveryId),
                tenant
            )
            .orElseThrow(() -> new NexraValidationException("Webhook delivery not found for replay."));
        if (delivery.getStatus() != IntegrationWebhookDeliveryStatus.DEAD_LETTER) {
            throw new NexraValidationException("Only dead-letter webhook deliveries can be replayed.");
        }
        if (delivery.getLastFailureAt() != null
            && delivery.getLastFailureAt().isAfter(Instant.now().minus(REPLAY_COOLDOWN_SECONDS, ChronoUnit.SECONDS))) {
            throw new NexraValidationException("Webhook delivery replay is cooling down. Retry after a short delay.");
        }

        delivery.setStatus(IntegrationWebhookDeliveryStatus.PENDING);
        delivery.setAttemptCount(0);
        delivery.setNextAttemptAt(Instant.now());
        delivery.setDeliveredAt(null);
        delivery.setLastStatusCode(null);
        delivery.setLastError(null);
        final IntegrationWebhookDeliveryEntity saved = webhookDeliveryRepository.save(delivery);
        auditEventService.record(AuditEventRecord.of(tenant, "CRM", "REPLAY_WEBHOOK_DELIVERY", "SUCCESS")
            .withActor(actorEmail, actorUserId)
            .withTarget("INTEGRATION_WEBHOOK_DELIVERY", saved.getId()));
        return toModel(saved);
    }

    @Override
    public IntegrationWebhookDeliveryMetrics getWebhookDeliveryMetrics(final String tenantCode) {
        final String tenant = normalizeTenant(tenantCode);
        final long pending = webhookDeliveryRepository.countByTenantCodeIgnoreCaseAndStatus(tenant, IntegrationWebhookDeliveryStatus.PENDING);
        final long retrying = webhookDeliveryRepository.countByTenantCodeIgnoreCaseAndStatus(tenant, IntegrationWebhookDeliveryStatus.RETRYING);
        final long succeeded = webhookDeliveryRepository.countByTenantCodeIgnoreCaseAndStatus(tenant, IntegrationWebhookDeliveryStatus.SUCCEEDED);
        final long deadLetter = webhookDeliveryRepository.countByTenantCodeIgnoreCaseAndStatus(tenant, IntegrationWebhookDeliveryStatus.DEAD_LETTER);
        return new IntegrationWebhookDeliveryMetrics(
            pending,
            retrying,
            succeeded,
            deadLetter,
            pending + retrying + succeeded + deadLetter
        );
    }

    @Override
    public IntegrationWebhookDeliveryAlertStatus getWebhookDeliveryAlertStatus(final String tenantCode) {
        final String tenant = normalizeTenant(tenantCode);
        final long retrying = webhookDeliveryRepository.countByTenantCodeIgnoreCaseAndStatus(tenant, IntegrationWebhookDeliveryStatus.RETRYING);
        final long deadLetter = webhookDeliveryRepository.countByTenantCodeIgnoreCaseAndStatus(tenant, IntegrationWebhookDeliveryStatus.DEAD_LETTER);
        final int retryingThreshold = crmProperties.getWebhook().getRetryingAlertThreshold();
        final int deadLetterThreshold = crmProperties.getWebhook().getDeadLetterAlertThreshold();
        return new IntegrationWebhookDeliveryAlertStatus(
            retrying,
            retryingThreshold,
            retrying >= retryingThreshold,
            deadLetter,
            deadLetterThreshold,
            deadLetter >= deadLetterThreshold
        );
    }

    @Override
    public List<IntegrationWebhookReplayAuditView> listWebhookReplayAudits(final String tenantCode, final int limit) {
        final String tenant = normalizeTenant(tenantCode);
        if (limit <= 0 || limit > 200) {
            throw new NexraValidationException("Replay audit limit must be between 1 and 200.");
        }
        return auditEventRepository.findByTenantModuleAndAction(tenant, "CRM", "REPLAY_WEBHOOK_DELIVERY", limit)
            .stream()
            .map(event -> new IntegrationWebhookReplayAuditView(
                event.getCreatedAt(),
                event.getActorEmail(),
                event.getActorUserId(),
                event.getTargetId(),
                event.getOutcome()
            ))
            .toList();
    }

    @Override
    public IntegrationWebhookSignatureVerification verifyWebhookSignature(
        final String tenantCode,
        final IntegrationWebhookSignatureVerificationRequest request
    ) {
        final String tenant = normalizeTenant(tenantCode);
        final String payloadJson = normalize(request.payloadJson());
        final String idempotencyKey = normalize(request.idempotencyKey());
        final String timestamp = normalize(request.timestamp());
        final long nowEpochSeconds = Instant.now().getEpochSecond();
        final long requestEpochSeconds = parseEpochSeconds(timestamp);
        final long skewSeconds = Math.max(30, crmProperties.getWebhook().getSignatureTimestampSkewSeconds());
        final boolean timestampValid = Math.abs(nowEpochSeconds - requestEpochSeconds) <= skewSeconds;
        final String expected = CrmWebhookSignatureCodec.buildSignature(
            CrmWebhookSignatureCodec.secretHash(request.secret()),
            payloadJson,
            idempotencyKey,
            timestamp
        );
        final boolean signatureValid = CrmWebhookSignatureCodec.matches(expected, normalize(request.signature()));
        final boolean replayDetected = timestampValid
            && signatureValid
            && !webhookReplayGuard.markIfFirstSeen(tenant, idempotencyKey, timestamp, normalize(request.signature()));
        final boolean valid = timestampValid && signatureValid && !replayDetected;
        return new IntegrationWebhookSignatureVerification(valid, CrmWebhookSignatureCodec.SIGNATURE_ALGORITHM, timestampValid, replayDetected);
    }

    private long parseEpochSeconds(final String timestamp) {
        try {
            return Long.parseLong(timestamp);
        } catch (NumberFormatException ex) {
            throw new NexraValidationException("Webhook timestamp must be epoch-seconds.");
        }
    }

    private URI validateWebhookUrl(final String targetUrl) {
        try {
            final URI uri = new URI(normalize(targetUrl));
            final String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
            if (!Set.of("http", "https").contains(scheme) || uri.getHost() == null || uri.getUserInfo() != null) {
                throw new NexraValidationException("Webhook targetUrl must be an http or https URL without embedded credentials.");
            }
            if (uri.getFragment() != null) {
                throw new NexraValidationException("Webhook targetUrl must not include URL fragments.");
            }
            final int port = uri.getPort();
            if (port != -1 && port != 80 && port != 443) {
                throw new NexraValidationException("Webhook targetUrl may only use port 80 or 443.");
            }
            if (isForbiddenWebhookHost(uri.getHost())) {
                throw new NexraValidationException("Webhook targetUrl host is not allowed.");
            }
            return uri;
        } catch (URISyntaxException ex) {
            throw new NexraValidationException("Webhook targetUrl is not a valid URI.");
        }
    }

    private boolean isForbiddenWebhookHost(final String host) {
        final String normalized = host.toLowerCase(Locale.ROOT).trim();
        if (normalized.equals("localhost") || normalized.endsWith(".localhost")) {
            return true;
        }
        if (normalized.equals("0.0.0.0") || normalized.equals("127.0.0.1") || normalized.equals("::1")) {
            return true;
        }
        if (normalized.startsWith("fc") || normalized.startsWith("fd")) {
            return true;
        }
        final String[] parts = normalized.split("\\.");
        if (parts.length != 4) {
            return false;
        }
        try {
            final int a = Integer.parseInt(parts[0]);
            final int b = Integer.parseInt(parts[1]);
            return a == 10
                || (a == 192 && b == 168)
                || (a == 169 && b == 254)
                || (a == 127)
                || (a == 172 && b >= 16 && b <= 31);
        } catch (NumberFormatException ex) {
            return false;
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

    private IntegrationWebhookDelivery toModel(final IntegrationWebhookDeliveryEntity entity) {
        return new IntegrationWebhookDelivery(
            entity.getId(),
            entity.getSubscription().getId(),
            entity.getEventType(),
            entity.getStatus().name(),
            entity.getAttemptCount(),
            entity.getMaxAttempts(),
            entity.getNextAttemptAt(),
            entity.getDeliveredAt(),
            entity.getLastFailureAt(),
            entity.getLastStatusCode(),
            entity.getLastError(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}
