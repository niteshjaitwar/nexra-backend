package com.nexra.hrms.nexra.modules.crm.service.impl;

import com.nexra.hrms.nexra.common.api.PageResponse;
import com.nexra.hrms.nexra.common.exception.NexraNotFoundException;
import com.nexra.hrms.nexra.common.exception.NexraValidationException;
import com.nexra.hrms.nexra.modules.crm.config.CrmProperties;
import com.nexra.hrms.nexra.modules.crm.dto.request.CrmLeadCreateRequest;
import com.nexra.hrms.nexra.modules.crm.dto.request.CrmLeadUpdateRequest;
import com.nexra.hrms.nexra.modules.crm.entity.CrmLeadEntity;
import com.nexra.hrms.nexra.modules.crm.model.CrmLead;
import com.nexra.hrms.nexra.modules.crm.model.CrmLeadStatus;
import com.nexra.hrms.nexra.modules.crm.repository.CrmLeadRepository;
import com.nexra.hrms.nexra.modules.crm.service.CrmLeadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;
import java.util.List;

/**
 * Persistent CRM lead service used as the initial CRM baseline.
 * This implementation keeps strict validation, tenant scoping, and deterministic behavior while
 * the wider CRM domain model is expanded in subsequent iterations.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CrmLeadServiceImpl implements CrmLeadService {

    private final CrmProperties properties;
    private final CrmLeadRepository repository;

    /**
     * Creates a new CRM lead with normalized text fields and NEW status.
     *
     * @param request lead creation payload.
     * @return created lead.
     */
    @Override
    public CrmLead create(final String tenantCode, final CrmLeadCreateRequest request) {
        final String normalizedTenantCode = normalizeTenantCode(tenantCode);
        final String normalizedEmail = normalize(request.email());
        ensureUniqueEmail(normalizedTenantCode, normalizedEmail, null);
        final Instant now = Instant.now();
        final CrmLeadEntity entity = new CrmLeadEntity();
        entity.setId(UUID.randomUUID().toString());
        entity.setTenantCode(normalizedTenantCode);
        entity.setFullName(normalize(request.fullName()));
        entity.setEmail(normalizedEmail);
        entity.setPhone(normalizeNullable(request.phone()));
        entity.setCompany(normalize(request.company()));
        entity.setSource(normalizeNullable(request.source()));
        entity.setOwnerUserId(normalize(request.ownerUserId()));
        entity.setNotes(normalizeNullable(request.notes()));
        entity.setStatus(CrmLeadStatus.NEW);
        entity.setDomainCreatedAt(now);
        entity.setDomainUpdatedAt(now);
        final CrmLeadEntity saved = repository.save(entity);
        log.info("CrmLeadServiceImpl - create() - leadId={}, ownerUserId={}", saved.getId(), saved.getOwnerUserId());
        return toModel(saved);
    }

    /**
     * Updates mutable lead fields and preserves immutable creation timestamp.
     *
     * @param leadId lead id.
     * @param request lead update payload.
     * @return updated lead.
     */
    @Override
    public CrmLead update(final String tenantCode, final String leadId, final CrmLeadUpdateRequest request) {
        final String normalizedTenantCode = normalizeTenantCode(tenantCode);
        final CrmLeadEntity entity = repository.findByIdAndTenantCodeIgnoreCase(leadId, normalizedTenantCode)
            .orElseThrow(() -> new NexraNotFoundException("CRM lead not found for id: " + leadId));
        final String email = request.email() != null ? normalize(request.email()) : entity.getEmail();
        ensureUniqueEmail(normalizedTenantCode, email, leadId);
        entity.setFullName(valueOrDefault(request.fullName(), entity.getFullName()));
        entity.setEmail(email);
        entity.setPhone(valueOrDefaultNullable(request.phone(), entity.getPhone()));
        entity.setCompany(valueOrDefault(request.company(), entity.getCompany()));
        entity.setSource(valueOrDefaultNullable(request.source(), entity.getSource()));
        entity.setOwnerUserId(valueOrDefault(request.ownerUserId(), entity.getOwnerUserId()));
        entity.setNotes(valueOrDefaultNullable(request.notes(), entity.getNotes()));
        entity.setStatus(request.status() != null ? request.status() : entity.getStatus());
        entity.setDomainUpdatedAt(Instant.now());
        final CrmLeadEntity saved = repository.save(entity);
        log.info("CrmLeadServiceImpl - update() - leadId={}, status={}", leadId, saved.getStatus());
        return toModel(saved);
    }

    /**
     * Finds a CRM lead by id.
     *
     * @param leadId lead id.
     * @return lead.
     */
    @Override
    public CrmLead findById(final String tenantCode, final String leadId) {
        final String normalizedTenantCode = normalizeTenantCode(tenantCode);
        final CrmLeadEntity entity = repository.findByIdAndTenantCodeIgnoreCase(leadId, normalizedTenantCode)
            .orElseThrow(() -> new NexraNotFoundException("CRM lead not found for id: " + leadId));
        return toModel(entity);
    }

    /**
     * Returns a stable paged list sorted by last updated timestamp descending.
     *
     * @param page zero-based page index.
     * @param size requested page size.
     * @return paged lead response.
     */
    @Override
    public PageResponse<CrmLead> list(final String tenantCode, final int page, final int size) {
        final String normalizedTenantCode = normalizeTenantCode(tenantCode);
        validatePaging(page, size);
        final Page<CrmLeadEntity> result = repository.findAllByTenantCodeIgnoreCase(normalizedTenantCode, PageRequest.of(page, size));
        final List<CrmLead> items = result.getContent().stream().map(this::toModel).toList();
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

    /**
     * Deletes a lead by id.
     *
     * @param leadId lead id.
     */
    @Override
    public void delete(final String tenantCode, final String leadId) {
        final String normalizedTenantCode = normalizeTenantCode(tenantCode);
        final CrmLeadEntity entity = repository.findByIdAndTenantCodeIgnoreCase(leadId, normalizedTenantCode)
            .orElseThrow(() -> new NexraNotFoundException("CRM lead not found for id: " + leadId));
        repository.delete(entity);
        log.info("CrmLeadServiceImpl - delete() - leadId={}", leadId);
    }

    /**
     * Validates pagination parameters against module constraints.
     *
     * @param page requested page.
     * @param size requested size.
     */
    private void validatePaging(final int page, final int size) {
        if (page < 0) {
            throw new NexraValidationException("Page index must be zero or greater.");
        }
        if (size <= 0) {
            throw new NexraValidationException("Page size must be greater than zero.");
        }
        if (size > properties.getMaxPageSize()) {
            throw new NexraValidationException("Page size must not exceed " + properties.getMaxPageSize() + ".");
        }
    }

    /**
     * Ensures lead emails are unique in a case-insensitive manner.
     *
     * @param email normalized email.
     * @param currentLeadId current lead id for update operations.
     */
    private void ensureUniqueEmail(final String tenantCode, final String email, final String currentLeadId) {
        final boolean exists = currentLeadId == null
            ? repository.existsByTenantCodeIgnoreCaseAndEmailIgnoreCase(tenantCode, email)
            : repository.existsByTenantCodeIgnoreCaseAndEmailIgnoreCaseAndIdNot(tenantCode, email, currentLeadId);
        if (exists) {
            throw new NexraValidationException("A lead with this email already exists.");
        }
    }

    private CrmLead toModel(final CrmLeadEntity entity) {
        return new CrmLead(
            entity.getId(),
            entity.getTenantCode(),
            entity.getFullName(),
            entity.getEmail(),
            entity.getPhone(),
            entity.getCompany(),
            entity.getSource(),
            entity.getOwnerUserId(),
            entity.getNotes(),
            entity.getStatus(),
            entity.getDomainCreatedAt(),
            entity.getDomainUpdatedAt()
        );
    }

    /**
     * Normalizes required text fields by trimming and enforcing non-empty values.
     *
     * @param value input value.
     * @return normalized value.
     */
    private String normalize(final String value) {
        if (value == null || value.isBlank()) {
            throw new NexraValidationException("Required field is missing.");
        }
        return value.trim();
    }

    /**
     * Normalizes optional text fields by trimming and collapsing blanks to null.
     *
     * @param value input value.
     * @return normalized value or null.
     */
    private String normalizeNullable(final String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    /**
     * Returns normalized new value when present, otherwise existing required value.
     *
     * @param candidate new candidate value.
     * @param existing existing value.
     * @return resolved value.
     */
    private String valueOrDefault(final String candidate, final String existing) {
        return candidate != null ? normalize(candidate) : existing;
    }

    /**
     * Returns normalized new optional value when present, otherwise existing value.
     *
     * @param candidate new candidate value.
     * @param existing existing value.
     * @return resolved value.
     */
    private String valueOrDefaultNullable(final String candidate, final String existing) {
        return candidate != null ? normalizeNullable(candidate) : existing;
    }

    private String normalizeTenantCode(final String value) {
        final String normalized = normalize(value);
        if (!normalized.matches("^[A-Za-z0-9_-]{2,60}$")) {
            throw new NexraValidationException("Tenant code must contain only letters, numbers, hyphen, or underscore.");
        }
        return normalized;
    }
}
