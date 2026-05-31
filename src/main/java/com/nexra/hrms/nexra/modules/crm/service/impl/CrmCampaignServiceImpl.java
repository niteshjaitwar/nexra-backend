package com.nexra.hrms.nexra.modules.crm.service.impl;

import com.nexra.hrms.nexra.common.api.PageResponse;
import com.nexra.hrms.nexra.common.audit.AuditEventRecord;
import com.nexra.hrms.nexra.common.audit.AuditEventService;
import com.nexra.hrms.nexra.common.exception.NexraNotFoundException;
import com.nexra.hrms.nexra.common.exception.NexraValidationException;
import com.nexra.hrms.nexra.modules.crm.config.CrmProperties;
import com.nexra.hrms.nexra.modules.crm.dto.request.CrmCampaignCreateRequest;
import com.nexra.hrms.nexra.modules.crm.dto.request.CrmCampaignStatusUpdateRequest;
import com.nexra.hrms.nexra.modules.crm.dto.request.CrmLeadCreateRequest;
import com.nexra.hrms.nexra.modules.crm.entity.CrmCampaignEntity;
import com.nexra.hrms.nexra.modules.crm.model.CrmCampaign;
import com.nexra.hrms.nexra.modules.crm.model.CrmLead;
import com.nexra.hrms.nexra.modules.crm.repository.CrmCampaignRepository;
import com.nexra.hrms.nexra.modules.crm.service.CrmCampaignService;
import com.nexra.hrms.nexra.modules.crm.service.CrmLeadService;
import com.nexra.hrms.nexra.modules.crm.support.CrmAccessScope;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default CRM campaign service. Enforces a configurable lifecycle state machine
 * and validates campaign types against externalized configuration. Records audit
 * events for create and status transitions.
 *
 * @author niteshjaitwar
 */
@Service
@RequiredArgsConstructor
public class CrmCampaignServiceImpl implements CrmCampaignService {

    private static final String AUDIT_MODULE = "CRM";

    private final CrmCampaignRepository campaignRepository;
    private final CrmLeadService leadService;
    private final CrmProperties crmProperties;
    private final AuditEventService auditEventService;

    @Override
    @Transactional
    public CrmCampaign create(final String tenantCode, final String actorEmail, final CrmCampaignCreateRequest request) {
        final String normalizedTenant = normalize(tenantCode);
        final CrmProperties.Campaign config = crmProperties.getCampaign();
        final String type = request.campaignType().trim().toUpperCase(Locale.ROOT);
        if (!config.getAllowedTypesNormalized().contains(type)) {
            throw new NexraValidationException("Unsupported campaign type: " + type);
        }
        if (request.startDate() != null && request.endDate() != null && request.endDate().isBefore(request.startDate())) {
            throw new NexraValidationException("Campaign end date cannot precede start date.");
        }

        final CrmCampaignEntity entity = new CrmCampaignEntity();
        entity.setId(UUID.randomUUID().toString());
        entity.setTenantCode(normalizedTenant);
        entity.setName(request.name().trim());
        entity.setCampaignType(type);
        entity.setStatus(config.getDefaultStatus());
        entity.setDescription(trimNullable(request.description()));
        entity.setBudget(request.budget());
        entity.setActualCost(request.actualCost());
        entity.setStartDate(request.startDate());
        entity.setEndDate(request.endDate());
        entity.setOwnerUserId(request.ownerUserId().trim());
        final CrmCampaignEntity saved = campaignRepository.save(entity);
        audit(normalizedTenant, actorEmail, "CREATE_CAMPAIGN", "SUCCESS", saved.getId(),
            "{\"type\":\"" + saved.getCampaignType() + "\",\"status\":\"" + saved.getStatus() + "\"}");
        return toModel(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CrmCampaign findById(final String tenantCode, final String campaignId) {
        return toModel(load(normalize(tenantCode), campaignId));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CrmCampaign> list(final String tenantCode, final int page, final int size) {
        final Page<CrmCampaignEntity> result = campaignRepository.findAllByTenantCodeIgnoreCaseOrderByUpdatedAtDescIdDesc(
            normalize(tenantCode),
            PageRequest.of(page, size)
        );
        final List<CrmCampaign> items = result.getContent().stream().map(this::toModel).toList();
        return new PageResponse<>(items, result.getNumber(), result.getSize(), result.getTotalElements(),
            result.getTotalPages(), result.hasNext(), result.hasPrevious());
    }

    @Override
    @Transactional
    public CrmCampaign transitionStatus(
        final String tenantCode,
        final String actorEmail,
        final String campaignId,
        final CrmCampaignStatusUpdateRequest request
    ) {
        final String normalizedTenant = normalize(tenantCode);
        final CrmCampaignEntity entity = load(normalizedTenant, campaignId);
        final CrmProperties.Campaign config = crmProperties.getCampaign();
        final String current = entity.getStatus();
        final String target = request.targetStatus().trim().toUpperCase(Locale.ROOT);

        if (!config.isKnownStatus(target)) {
            audit(normalizedTenant, actorEmail, "CAMPAIGN_STATUS_TRANSITION", "FAILURE", entity.getId(),
                "{\"reason\":\"UNKNOWN_STATUS\",\"target\":\"" + target + "\"}");
            throw new NexraValidationException("Unknown campaign status: " + target);
        }
        if (!config.isTransitionAllowed(current, target)) {
            audit(normalizedTenant, actorEmail, "CAMPAIGN_STATUS_TRANSITION", "FAILURE", entity.getId(),
                "{\"reason\":\"ILLEGAL_TRANSITION\",\"from\":\"" + current + "\",\"to\":\"" + target + "\"}");
            throw new NexraValidationException("Illegal campaign status transition from " + current + " to " + target + ".");
        }

        entity.setStatus(target);
        final CrmCampaignEntity saved = campaignRepository.save(entity);
        audit(normalizedTenant, actorEmail, "CAMPAIGN_STATUS_TRANSITION", "SUCCESS", saved.getId(),
            "{\"from\":\"" + current + "\",\"to\":\"" + target + "\"}");
        return toModel(saved);
    }

    @Override
    @Transactional
    public CrmLead captureLead(
        final String tenantCode,
        final String campaignId,
        final String actorEmail,
        final CrmLeadCreateRequest request,
        final CrmAccessScope accessScope
    ) {
        final String normalizedTenant = normalize(tenantCode);
        load(normalizedTenant, campaignId);
        final CrmLeadCreateRequest attributed = new CrmLeadCreateRequest(
            request.fullName(),
            request.email(),
            request.phone(),
            request.company(),
            request.source(),
            campaignId,
            request.ownerUserId(),
            request.notes()
        );
        final CrmLead lead = leadService.create(normalizedTenant, attributed, accessScope);
        audit(normalizedTenant, actorEmail, "CAMPAIGN_LEAD_CAPTURED", "SUCCESS", campaignId,
            "{\"leadId\":\"" + lead.id() + "\"}");
        return lead;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CrmLead> listCampaignLeads(
        final String tenantCode,
        final String campaignId,
        final int page,
        final int size,
        final CrmAccessScope accessScope
    ) {
        load(normalize(tenantCode), campaignId);
        return leadService.listByCampaign(tenantCode, campaignId, page, size, accessScope);
    }

    private CrmCampaignEntity load(final String tenantCode, final String campaignId) {
        return campaignRepository.findByIdAndTenantCodeIgnoreCase(campaignId, tenantCode)
            .orElseThrow(() -> new NexraNotFoundException("CRM campaign not found for id: " + campaignId));
    }

    private void audit(
        final String tenantCode,
        final String actorEmail,
        final String action,
        final String outcome,
        final String campaignId,
        final String detailJson
    ) {
        auditEventService.record(AuditEventRecord
            .of(tenantCode, AUDIT_MODULE, action, outcome)
            .withActor(actorEmail, null)
            .withTarget("CRM_CAMPAIGN", campaignId)
            .withDetail(detailJson));
    }

    private CrmCampaign toModel(final CrmCampaignEntity entity) {
        return new CrmCampaign(
            entity.getId(),
            entity.getTenantCode(),
            entity.getName(),
            entity.getCampaignType(),
            entity.getStatus(),
            entity.getDescription(),
            entity.getBudget(),
            entity.getActualCost(),
            entity.getStartDate(),
            entity.getEndDate(),
            entity.getOwnerUserId()
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
