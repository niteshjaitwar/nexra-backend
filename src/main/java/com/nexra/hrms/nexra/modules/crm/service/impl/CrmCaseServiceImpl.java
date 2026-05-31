package com.nexra.hrms.nexra.modules.crm.service.impl;

import com.nexra.hrms.nexra.common.api.PageResponse;
import com.nexra.hrms.nexra.common.audit.AuditEventRecord;
import com.nexra.hrms.nexra.common.audit.AuditEventService;
import com.nexra.hrms.nexra.common.exception.NexraNotFoundException;
import com.nexra.hrms.nexra.common.exception.NexraValidationException;
import com.nexra.hrms.nexra.modules.crm.config.CrmProperties;
import com.nexra.hrms.nexra.modules.crm.dto.request.CrmCaseAssignRequest;
import com.nexra.hrms.nexra.modules.crm.dto.request.CrmCaseCreateRequest;
import com.nexra.hrms.nexra.modules.crm.dto.request.CrmCaseStatusUpdateRequest;
import com.nexra.hrms.nexra.modules.crm.entity.CrmCaseEntity;
import com.nexra.hrms.nexra.modules.crm.model.CrmCase;
import com.nexra.hrms.nexra.modules.crm.repository.CrmCaseRepository;
import com.nexra.hrms.nexra.modules.crm.service.CrmCaseService;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default CRM case service. Enforces a configurable status state machine and
 * records audit events for every create, status transition, and reassignment.
 *
 * @author niteshjaitwar
 */
@Service
@RequiredArgsConstructor
public class CrmCaseServiceImpl implements CrmCaseService {

    private static final String AUDIT_MODULE = "CRM";

    private final CrmCaseRepository caseRepository;
    private final CrmProperties crmProperties;
    private final AuditEventService auditEventService;

    @Override
    @Transactional
    public CrmCase create(final String tenantCode, final String actorEmail, final CrmCaseCreateRequest request) {
        final String normalizedTenant = normalize(tenantCode);
        final CrmProperties.Case caseConfig = crmProperties.getCaseConfig();
        final CrmCaseEntity entity = new CrmCaseEntity();
        entity.setId(UUID.randomUUID().toString());
        entity.setTenantCode(normalizedTenant);
        entity.setSubject(request.subject().trim());
        entity.setDescription(trimNullable(request.description()));
        entity.setStatus(caseConfig.getDefaultStatus());
        entity.setPriority(request.priority() == null || request.priority().isBlank()
            ? caseConfig.getDefaultPriority()
            : request.priority().trim().toUpperCase(Locale.ROOT));
        entity.setAccountId(trimNullable(request.accountId()));
        entity.setContactId(trimNullable(request.contactId()));
        entity.setOwnerUserId(request.ownerUserId().trim());
        entity.setSlaDueAt(request.slaDueAt());
        final CrmCaseEntity saved = caseRepository.save(entity);
        audit(normalizedTenant, actorEmail, "CREATE_CASE", "SUCCESS", saved.getId(),
            "{\"status\":\"" + saved.getStatus() + "\"}");
        return toModel(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CrmCase findById(final String tenantCode, final String caseId) {
        return toModel(load(normalize(tenantCode), caseId));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CrmCase> list(final String tenantCode, final int page, final int size) {
        final Page<CrmCaseEntity> result = caseRepository.findAllByTenantCodeIgnoreCaseOrderByUpdatedAtDescIdDesc(
            normalize(tenantCode),
            PageRequest.of(page, size)
        );
        final List<CrmCase> items = result.getContent().stream().map(this::toModel).toList();
        return new PageResponse<>(items, result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages(), result.hasNext(), result.hasPrevious());
    }

    @Override
    @Transactional
    public CrmCase transitionStatus(
        final String tenantCode,
        final String actorEmail,
        final String caseId,
        final CrmCaseStatusUpdateRequest request
    ) {
        final String normalizedTenant = normalize(tenantCode);
        final CrmCaseEntity entity = load(normalizedTenant, caseId);
        final CrmProperties.Case caseConfig = crmProperties.getCaseConfig();
        final String current = entity.getStatus();
        final String target = request.targetStatus().trim().toUpperCase(Locale.ROOT);

        if (!caseConfig.isKnownStatus(target)) {
            audit(normalizedTenant, actorEmail, "CASE_STATUS_TRANSITION", "FAILURE", entity.getId(),
                "{\"reason\":\"UNKNOWN_STATUS\",\"target\":\"" + target + "\"}");
            throw new NexraValidationException("Unknown case status: " + target);
        }
        if (!caseConfig.isTransitionAllowed(current, target)) {
            audit(normalizedTenant, actorEmail, "CASE_STATUS_TRANSITION", "FAILURE", entity.getId(),
                "{\"reason\":\"ILLEGAL_TRANSITION\",\"from\":\"" + current + "\",\"to\":\"" + target + "\"}");
            throw new NexraValidationException("Illegal case status transition from " + current + " to " + target + ".");
        }

        entity.setStatus(target);
        final CrmCaseEntity saved = caseRepository.save(entity);
        audit(normalizedTenant, actorEmail, "CASE_STATUS_TRANSITION", "SUCCESS", saved.getId(),
            "{\"from\":\"" + current + "\",\"to\":\"" + target + "\"}");
        return toModel(saved);
    }

    @Override
    @Transactional
    public CrmCase assign(
        final String tenantCode,
        final String actorEmail,
        final String caseId,
        final CrmCaseAssignRequest request
    ) {
        final String normalizedTenant = normalize(tenantCode);
        final CrmCaseEntity entity = load(normalizedTenant, caseId);
        final String previousOwner = entity.getOwnerUserId();
        entity.setOwnerUserId(request.ownerUserId().trim());
        final CrmCaseEntity saved = caseRepository.save(entity);
        audit(normalizedTenant, actorEmail, "CASE_REASSIGNED", "SUCCESS", saved.getId(),
            "{\"from\":\"" + previousOwner + "\",\"to\":\"" + saved.getOwnerUserId() + "\"}");
        return toModel(saved);
    }

    private CrmCaseEntity load(final String tenantCode, final String caseId) {
        return caseRepository.findByIdAndTenantCodeIgnoreCase(caseId, tenantCode)
            .orElseThrow(() -> new NexraNotFoundException("CRM case not found for id: " + caseId));
    }

    private void audit(
        final String tenantCode,
        final String actorEmail,
        final String action,
        final String outcome,
        final String caseId,
        final String detailJson
    ) {
        auditEventService.record(AuditEventRecord
            .of(tenantCode, AUDIT_MODULE, action, outcome)
            .withActor(actorEmail, null)
            .withTarget("CRM_CASE", caseId)
            .withDetail(detailJson));
    }

    private CrmCase toModel(final CrmCaseEntity entity) {
        return new CrmCase(
            entity.getId(),
            entity.getTenantCode(),
            entity.getSubject(),
            entity.getDescription(),
            entity.getStatus(),
            entity.getPriority(),
            entity.getAccountId(),
            entity.getContactId(),
            entity.getOwnerUserId(),
            entity.getSlaDueAt()
        );
    }

    private String normalize(final String tenantCode) {
        if (tenantCode == null || tenantCode.isBlank()) {
            throw new NexraValidationException("Tenant code is required.");
        }
        return tenantCode.trim();
    }

    private String trimNullable(final String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
