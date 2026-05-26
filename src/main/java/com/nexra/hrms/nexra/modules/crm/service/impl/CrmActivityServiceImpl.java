package com.nexra.hrms.nexra.modules.crm.service.impl;

import com.nexra.hrms.nexra.common.api.PageResponse;
import com.nexra.hrms.nexra.common.audit.AuditEventRecord;
import com.nexra.hrms.nexra.common.audit.AuditEventService;
import com.nexra.hrms.nexra.common.exception.NexraNotFoundException;
import com.nexra.hrms.nexra.common.exception.NexraValidationException;
import com.nexra.hrms.nexra.modules.crm.config.CrmProperties;
import com.nexra.hrms.nexra.modules.crm.dto.request.CrmActivityCreateRequest;
import com.nexra.hrms.nexra.modules.crm.entity.CrmActivityEntity;
import com.nexra.hrms.nexra.modules.crm.model.CrmActivity;
import com.nexra.hrms.nexra.modules.crm.repository.CrmActivityRepository;
import com.nexra.hrms.nexra.modules.crm.repository.CrmContactRepository;
import com.nexra.hrms.nexra.modules.crm.repository.CrmDealRepository;
import com.nexra.hrms.nexra.modules.crm.repository.CrmLeadRepository;
import com.nexra.hrms.nexra.modules.crm.service.CrmActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CrmActivityServiceImpl implements CrmActivityService {

    private static final Set<String> USER_ACTIVITY_TYPES = Set.of("CALL", "EMAIL", "MEETING", "NOTE", "TASK");

    private final CrmActivityRepository activityRepository;
    private final CrmLeadRepository leadRepository;
    private final CrmContactRepository contactRepository;
    private final CrmDealRepository dealRepository;
    private final CrmProperties properties;
    private final AuditEventService auditEventService;

    @Override
    public CrmActivity create(final String tenantCode, final CrmActivityCreateRequest request) {
        final String tenant = normalize(tenantCode);
        final String activityType = normalize(request.activityType()).toUpperCase(Locale.ROOT);
        if (!USER_ACTIVITY_TYPES.contains(activityType)) {
            throw new NexraValidationException("Unsupported CRM activity type.");
        }
        validateLinkedRecord(tenant, request);

        final CrmActivityEntity entity = new CrmActivityEntity();
        entity.setId(UUID.randomUUID().toString());
        entity.setTenantCode(tenant);
        entity.setLeadId(normalizeNullable(request.leadId()));
        entity.setContactId(normalizeNullable(request.contactId()));
        entity.setDealId(normalizeNullable(request.dealId()));
        entity.setActivityType(activityType);
        entity.setNotes(normalizeNullable(request.notes()));
        entity.setOccurredAt(request.occurredAt() != null ? request.occurredAt() : Instant.now());
        entity.setOwnerUserId(normalize(request.ownerUserId()));

        final CrmActivityEntity saved = activityRepository.save(entity);
        auditEventService.record(AuditEventRecord.of(tenant, "CRM", "CREATE_ACTIVITY", "SUCCESS")
            .withActor(saved.getOwnerUserId(), null)
            .withTarget("CRM_ACTIVITY", saved.getId())
            .withDetail("{\"activityType\":\"" + saved.getActivityType() + "\"}"));
        return toModel(saved);
    }

    @Override
    public PageResponse<CrmActivity> list(
        final String tenantCode,
        final String leadId,
        final String contactId,
        final String dealId,
        final int page,
        final int size
    ) {
        final String tenant = normalize(tenantCode);
        validatePaging(page, size);
        validateAtMostOneFilter(leadId, contactId, dealId);

        final PageRequest pageRequest = PageRequest.of(page, size);
        final Page<CrmActivityEntity> result;
        if (hasText(leadId)) {
            result = activityRepository.findAllByTenantCodeIgnoreCaseAndLeadIdOrderByOccurredAtDescIdDesc(
                tenant, leadId.trim(), pageRequest);
        } else if (hasText(contactId)) {
            result = activityRepository.findAllByTenantCodeIgnoreCaseAndContactIdOrderByOccurredAtDescIdDesc(
                tenant, contactId.trim(), pageRequest);
        } else if (hasText(dealId)) {
            result = activityRepository.findAllByTenantCodeIgnoreCaseAndDealIdOrderByOccurredAtDescIdDesc(
                tenant, dealId.trim(), pageRequest);
        } else {
            result = activityRepository.findAllByTenantCodeIgnoreCaseOrderByOccurredAtDescIdDesc(tenant, pageRequest);
        }

        final List<CrmActivity> items = result.getContent().stream().map(this::toModel).toList();
        return new PageResponse<>(
            items,
            result.getNumber(),
            result.getSize(),
            result.getTotalElements(),
            result.getTotalPages(),
            result.hasNext(),
            result.hasPrevious()
        );
    }

    private void validateLinkedRecord(final String tenantCode, final CrmActivityCreateRequest request) {
        if (hasText(request.leadId())) {
            leadRepository.findByIdAndTenantCodeIgnoreCase(request.leadId().trim(), tenantCode)
                .orElseThrow(() -> new NexraNotFoundException("CRM lead not found for id: " + request.leadId()));
        } else if (hasText(request.contactId())) {
            contactRepository.findByIdAndTenantCodeIgnoreCase(request.contactId().trim(), tenantCode)
                .orElseThrow(() -> new NexraNotFoundException("CRM contact not found for id: " + request.contactId()));
        } else if (hasText(request.dealId())) {
            dealRepository.findByIdAndTenantCodeIgnoreCase(request.dealId().trim(), tenantCode)
                .orElseThrow(() -> new NexraNotFoundException("CRM deal not found for id: " + request.dealId()));
        }
    }

    private void validatePaging(final int page, final int size) {
        if (page < 0) {
            throw new NexraValidationException("Page index must be zero or greater.");
        }
        if (size <= 0 || size > properties.getMaxPageSize()) {
            throw new NexraValidationException("Page size must be between 1 and " + properties.getMaxPageSize() + ".");
        }
    }

    private void validateAtMostOneFilter(final String leadId, final String contactId, final String dealId) {
        int count = 0;
        count += hasText(leadId) ? 1 : 0;
        count += hasText(contactId) ? 1 : 0;
        count += hasText(dealId) ? 1 : 0;
        if (count > 1) {
            throw new NexraValidationException("Filter by at most one of leadId, contactId, or dealId.");
        }
    }

    private CrmActivity toModel(final CrmActivityEntity entity) {
        return new CrmActivity(
            entity.getId(),
            entity.getTenantCode(),
            entity.getLeadId(),
            entity.getContactId(),
            entity.getDealId(),
            entity.getActivityType(),
            entity.getNotes(),
            entity.getOccurredAt(),
            entity.getOwnerUserId(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    private String normalize(final String value) {
        if (value == null || value.isBlank()) {
            throw new NexraValidationException("Required field is missing.");
        }
        return value.trim();
    }

    private String normalizeNullable(final String value) {
        return hasText(value) ? value.trim() : null;
    }

    private boolean hasText(final String value) {
        return value != null && !value.isBlank();
    }
}
