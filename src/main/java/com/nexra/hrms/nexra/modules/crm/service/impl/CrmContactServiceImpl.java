package com.nexra.hrms.nexra.modules.crm.service.impl;

import com.nexra.hrms.nexra.common.api.PageResponse;
import com.nexra.hrms.nexra.common.audit.AuditEventRecord;
import com.nexra.hrms.nexra.common.audit.AuditEventService;
import com.nexra.hrms.nexra.common.exception.NexraForbiddenException;
import com.nexra.hrms.nexra.common.exception.NexraNotFoundException;
import com.nexra.hrms.nexra.common.exception.NexraValidationException;
import com.nexra.hrms.nexra.modules.crm.config.CrmProperties;
import com.nexra.hrms.nexra.modules.crm.dto.request.CrmContactCreateRequest;
import com.nexra.hrms.nexra.modules.crm.dto.request.CrmContactUpdateRequest;
import com.nexra.hrms.nexra.modules.crm.entity.CrmContactEntity;
import com.nexra.hrms.nexra.modules.crm.model.CrmContact;
import com.nexra.hrms.nexra.modules.crm.repository.CrmAccountRepository;
import com.nexra.hrms.nexra.modules.crm.repository.CrmContactRepository;
import com.nexra.hrms.nexra.modules.crm.service.CrmContactService;
import com.nexra.hrms.nexra.modules.crm.support.CrmAccessScope;
import com.nexra.hrms.nexra.modules.crm.support.CrmCustomFieldSupport;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CrmContactServiceImpl implements CrmContactService {

    private final CrmContactRepository repository;
    private final CrmAccountRepository accountRepository;
    private final CrmProperties properties;
    private final AuditEventService auditEventService;
    private final CrmCustomFieldSupport customFieldSupport;

    @Override
    @Transactional
    public CrmContact create(final String tenantCode, final CrmContactCreateRequest request, final CrmAccessScope accessScope) {
        enforceOwnerAccess(accessScope, request.ownerUserId());
        final String normalizedTenant = normalize(tenantCode);
        validateAccountIfPresent(normalizedTenant, request.accountId());
        final CrmContactEntity entity = new CrmContactEntity();
        entity.setId(UUID.randomUUID().toString());
        entity.setTenantCode(normalizedTenant);
        entity.setAccountId(normalizeNullable(request.accountId()));
        entity.setFullName(normalize(request.fullName()));
        entity.setEmail(normalizeNullable(request.email()));
        entity.setPhone(normalizeNullable(request.phone()));
        entity.setOwnerUserId(normalize(request.ownerUserId()));
        final CrmContactEntity saved = repository.save(entity);
        customFieldSupport.upsertValues(
            normalizedTenant,
            CrmCustomFieldSupport.MODULE_CRM_CONTACTS,
            saved.getId(),
            request.customFields()
        );
        auditEventService.record(AuditEventRecord.of(saved.getTenantCode(), "CRM", "CREATE_CONTACT", "SUCCESS")
            .withActor(saved.getOwnerUserId(), null)
            .withTarget("CRM_CONTACT", saved.getId()));
        return toModel(saved);
    }

    @Override
    @Transactional
    public CrmContact update(
        final String tenantCode,
        final String contactId,
        final CrmContactUpdateRequest request,
        final CrmAccessScope accessScope
    ) {
        final CrmContactEntity entity = loadVisible(contactId, tenantCode, accessScope);
        if (request.ownerUserId() != null) {
            enforceOwnerAccess(accessScope, request.ownerUserId());
        }
        if (request.accountId() != null) {
            validateAccountIfPresent(entity.getTenantCode(), request.accountId());
        }
        entity.setAccountId(valueOrDefaultNullable(request.accountId(), entity.getAccountId()));
        entity.setFullName(valueOrDefault(request.fullName(), entity.getFullName()));
        entity.setEmail(valueOrDefaultNullable(request.email(), entity.getEmail()));
        entity.setPhone(valueOrDefaultNullable(request.phone(), entity.getPhone()));
        entity.setOwnerUserId(valueOrDefault(request.ownerUserId(), entity.getOwnerUserId()));
        final CrmContactEntity saved = repository.save(entity);
        if (request.customFields() != null) {
            customFieldSupport.upsertValues(
                saved.getTenantCode(),
                CrmCustomFieldSupport.MODULE_CRM_CONTACTS,
                saved.getId(),
                request.customFields()
            );
        }
        auditEventService.record(AuditEventRecord.of(saved.getTenantCode(), "CRM", "UPDATE_CONTACT", "SUCCESS")
            .withActor(saved.getOwnerUserId(), null)
            .withTarget("CRM_CONTACT", saved.getId()));
        return toModel(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CrmContact findById(final String tenantCode, final String contactId, final CrmAccessScope accessScope) {
        return toModel(loadVisible(contactId, tenantCode, accessScope));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CrmContact> list(final String tenantCode, final int page, final int size, final CrmAccessScope accessScope) {
        validatePaging(page, size);
        final String normalizedTenant = normalize(tenantCode);
        final Page<CrmContactEntity> result = accessScope.privileged()
            ? repository.findAllByTenantCodeIgnoreCaseOrderByUpdatedAtDescIdDesc(normalizedTenant, PageRequest.of(page, size))
            : repository.findAllByTenantCodeIgnoreCaseAndOwnerUserIdOrderByUpdatedAtDescIdDesc(
                normalizedTenant,
                requireActorUserId(accessScope),
                PageRequest.of(page, size)
            );
        final List<CrmContact> items = result.getContent().stream().map(this::toModel).toList();
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

    @Override
    @Transactional
    public void delete(final String tenantCode, final String contactId, final CrmAccessScope accessScope) {
        final CrmContactEntity entity = loadVisible(contactId, tenantCode, accessScope);
        repository.delete(entity);
        auditEventService.record(AuditEventRecord.of(entity.getTenantCode(), "CRM", "DELETE_CONTACT", "SUCCESS")
            .withActor(entity.getOwnerUserId(), null)
            .withTarget("CRM_CONTACT", contactId));
    }

    private CrmContactEntity loadVisible(final String contactId, final String tenantCode, final CrmAccessScope accessScope) {
        final CrmContactEntity entity = repository.findByIdAndTenantCodeIgnoreCase(contactId, normalize(tenantCode))
            .orElseThrow(() -> new NexraNotFoundException("CRM contact not found for id: " + contactId));
        ensureVisibleForAccessScope(entity, accessScope, contactId);
        return entity;
    }

    private void validateAccountIfPresent(final String tenantCode, final String accountId) {
        if (accountId == null || accountId.isBlank()) {
            return;
        }
        final String normalizedAccountId = accountId.trim();
        accountRepository.findByIdAndTenantCodeIgnoreCase(normalizedAccountId, tenantCode)
            .orElseThrow(() -> new NexraValidationException("CRM account not found for id: " + normalizedAccountId));
    }

    private CrmContact toModel(final CrmContactEntity entity) {
        final Map<String, Object> customFields = customFieldSupport.readValues(
            entity.getTenantCode(),
            CrmCustomFieldSupport.MODULE_CRM_CONTACTS,
            entity.getId()
        );
        return new CrmContact(
            entity.getId(),
            entity.getTenantCode(),
            entity.getAccountId(),
            entity.getFullName(),
            entity.getEmail(),
            entity.getPhone(),
            entity.getOwnerUserId(),
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            customFields
        );
    }

    private void validatePaging(final int page, final int size) {
        if (page < 0) {
            throw new NexraValidationException("Page index must be zero or greater.");
        }
        if (size <= 0 || size > properties.getMaxPageSize()) {
            throw new NexraValidationException("Page size must be between 1 and " + properties.getMaxPageSize() + ".");
        }
    }

    private String normalize(final String value) {
        if (value == null || value.isBlank()) {
            throw new NexraValidationException("Required field is missing.");
        }
        return value.trim();
    }

    private String normalizeNullable(final String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }

    private String valueOrDefault(final String candidate, final String existing) {
        return candidate != null ? normalize(candidate) : existing;
    }

    private String valueOrDefaultNullable(final String candidate, final String existing) {
        return candidate != null ? normalizeNullable(candidate) : existing;
    }

    private void ensureVisibleForAccessScope(final CrmContactEntity entity, final CrmAccessScope accessScope, final String contactId) {
        if (accessScope.privileged()) {
            return;
        }
        final String actorUserId = requireActorUserId(accessScope);
        if (actorUserId.equals(entity.getOwnerUserId())) {
            return;
        }
        throw new NexraNotFoundException("CRM contact not found for id: " + contactId);
    }

    private void enforceOwnerAccess(final CrmAccessScope accessScope, final String targetOwnerUserId) {
        if (accessScope.privileged()) {
            return;
        }
        final String actorUserId = requireActorUserId(accessScope);
        if (actorUserId.equals(normalize(targetOwnerUserId))) {
            return;
        }
        throw new NexraForbiddenException("Non-admin CRM users can only operate on their own owned records.");
    }

    private String requireActorUserId(final CrmAccessScope accessScope) {
        if (accessScope.actorUserId() == null || accessScope.actorUserId().isBlank()) {
            throw new NexraForbiddenException("Authenticated CRM user is missing actor identity.");
        }
        return accessScope.actorUserId().trim();
    }
}
