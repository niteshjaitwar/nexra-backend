package com.nexra.hrms.nexra.modules.crm.service.impl;

import com.nexra.hrms.nexra.common.api.PageResponse;
import com.nexra.hrms.nexra.common.audit.AuditEventRecord;
import com.nexra.hrms.nexra.common.audit.AuditEventService;
import com.nexra.hrms.nexra.common.exception.NexraForbiddenException;
import com.nexra.hrms.nexra.common.exception.NexraNotFoundException;
import com.nexra.hrms.nexra.common.exception.NexraValidationException;
import com.nexra.hrms.nexra.modules.crm.config.CrmProperties;
import com.nexra.hrms.nexra.modules.crm.dto.request.CrmAccountCreateRequest;
import com.nexra.hrms.nexra.modules.crm.dto.request.CrmAccountUpdateRequest;
import com.nexra.hrms.nexra.modules.crm.entity.CrmAccountEntity;
import com.nexra.hrms.nexra.modules.crm.model.CrmAccount;
import com.nexra.hrms.nexra.modules.crm.repository.CrmAccountRepository;
import com.nexra.hrms.nexra.modules.crm.service.CrmAccountService;
import com.nexra.hrms.nexra.modules.crm.support.CrmAccessScope;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CrmAccountServiceImpl implements CrmAccountService {

    private final CrmAccountRepository repository;
    private final CrmProperties properties;
    private final AuditEventService auditEventService;

    @Override
    public CrmAccount create(final String tenantCode, final CrmAccountCreateRequest request, final CrmAccessScope accessScope) {
        enforceOwnerAccess(accessScope, request.ownerUserId());
        final CrmAccountEntity entity = new CrmAccountEntity();
        entity.setId(UUID.randomUUID().toString());
        entity.setTenantCode(normalize(tenantCode));
        entity.setName(normalize(request.name()));
        entity.setWebsite(normalizeNullable(request.website()));
        entity.setIndustry(normalizeNullable(request.industry()));
        entity.setOwnerUserId(normalize(request.ownerUserId()));
        final CrmAccountEntity saved = repository.save(entity);
        auditEventService.record(AuditEventRecord.of(saved.getTenantCode(), "CRM", "CREATE_ACCOUNT", "SUCCESS")
            .withActor(saved.getOwnerUserId(), null)
            .withTarget("CRM_ACCOUNT", saved.getId()));
        return toModel(saved);
    }

    @Override
    public CrmAccount update(
        final String tenantCode,
        final String accountId,
        final CrmAccountUpdateRequest request,
        final CrmAccessScope accessScope
    ) {
        final CrmAccountEntity entity = repository.findByIdAndTenantCodeIgnoreCase(accountId, normalize(tenantCode))
            .orElseThrow(() -> new NexraNotFoundException("CRM account not found for id: " + accountId));
        ensureVisibleForAccessScope(entity, accessScope, accountId);
        if (request.ownerUserId() != null) {
            enforceOwnerAccess(accessScope, request.ownerUserId());
        }
        entity.setName(valueOrDefault(request.name(), entity.getName()));
        entity.setWebsite(valueOrDefaultNullable(request.website(), entity.getWebsite()));
        entity.setIndustry(valueOrDefaultNullable(request.industry(), entity.getIndustry()));
        entity.setOwnerUserId(valueOrDefault(request.ownerUserId(), entity.getOwnerUserId()));
        final CrmAccountEntity saved = repository.save(entity);
        auditEventService.record(AuditEventRecord.of(saved.getTenantCode(), "CRM", "UPDATE_ACCOUNT", "SUCCESS")
            .withActor(saved.getOwnerUserId(), null)
            .withTarget("CRM_ACCOUNT", saved.getId()));
        return toModel(saved);
    }

    @Override
    public CrmAccount findById(final String tenantCode, final String accountId, final CrmAccessScope accessScope) {
        final CrmAccountEntity entity = repository.findByIdAndTenantCodeIgnoreCase(accountId, normalize(tenantCode))
            .orElseThrow(() -> new NexraNotFoundException("CRM account not found for id: " + accountId));
        ensureVisibleForAccessScope(entity, accessScope, accountId);
        return toModel(entity);
    }

    @Override
    public PageResponse<CrmAccount> list(final String tenantCode, final int page, final int size, final CrmAccessScope accessScope) {
        validatePaging(page, size);
        final String normalizedTenantCode = normalize(tenantCode);
        final Page<CrmAccountEntity> result = accessScope.privileged()
            ? repository.findAllByTenantCodeIgnoreCaseOrderByUpdatedAtDescIdDesc(normalizedTenantCode, PageRequest.of(page, size))
            : repository.findAllByTenantCodeIgnoreCaseAndOwnerUserIdOrderByUpdatedAtDescIdDesc(
                normalizedTenantCode,
                requireActorUserId(accessScope),
                PageRequest.of(page, size)
            );
        final List<CrmAccount> items = result.getContent().stream().map(this::toModel).toList();
        return new PageResponse<>(items, result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages(), result.hasNext(), result.hasPrevious());
    }

    @Override
    public void delete(final String tenantCode, final String accountId, final CrmAccessScope accessScope) {
        final CrmAccountEntity entity = repository.findByIdAndTenantCodeIgnoreCase(accountId, normalize(tenantCode))
            .orElseThrow(() -> new NexraNotFoundException("CRM account not found for id: " + accountId));
        ensureVisibleForAccessScope(entity, accessScope, accountId);
        repository.delete(entity);
        auditEventService.record(AuditEventRecord.of(entity.getTenantCode(), "CRM", "DELETE_ACCOUNT", "SUCCESS")
            .withActor(entity.getOwnerUserId(), null)
            .withTarget("CRM_ACCOUNT", accountId));
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

    private CrmAccount toModel(final CrmAccountEntity entity) {
        return new CrmAccount(
            entity.getId(),
            entity.getTenantCode(),
            entity.getName(),
            entity.getWebsite(),
            entity.getIndustry(),
            entity.getOwnerUserId(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    private void ensureVisibleForAccessScope(final CrmAccountEntity entity, final CrmAccessScope accessScope, final String accountId) {
        if (accessScope.privileged()) {
            return;
        }
        final String actorUserId = requireActorUserId(accessScope);
        if (actorUserId.equals(entity.getOwnerUserId())) {
            return;
        }
        throw new NexraNotFoundException("CRM account not found for id: " + accountId);
    }

    private void enforceOwnerAccess(final CrmAccessScope accessScope, final String targetOwnerUserId) {
        if (accessScope.privileged()) {
            return;
        }
        final String actorUserId = requireActorUserId(accessScope);
        final String targetOwner = normalize(targetOwnerUserId);
        if (actorUserId.equals(targetOwner)) {
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
