package com.nexra.hrms.nexra.modules.operations.service.impl;

import com.nexra.hrms.nexra.common.api.PageResponse;
import com.nexra.hrms.nexra.common.exception.NexraNotFoundException;
import com.nexra.hrms.nexra.common.exception.NexraValidationException;
import com.nexra.hrms.nexra.modules.crm.entity.CrmDealEntity;
import com.nexra.hrms.nexra.modules.crm.repository.CrmDealRepository;
import com.nexra.hrms.nexra.modules.operations.dto.OpsProjectCreateRequest;
import com.nexra.hrms.nexra.modules.operations.entity.OpsProjectEntity;
import com.nexra.hrms.nexra.modules.operations.model.OpsProject;
import com.nexra.hrms.nexra.modules.operations.config.OperationsProperties;
import com.nexra.hrms.nexra.modules.operations.repository.OpsProjectRepository;
import com.nexra.hrms.nexra.modules.operations.service.OpsProjectService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OpsProjectServiceImpl implements OpsProjectService {

    private final OpsProjectRepository projectRepository;
    private final CrmDealRepository crmDealRepository;
    private final OperationsProperties operationsProperties;

    @Override
    @Transactional
    public OpsProject create(final String tenantCode, final OpsProjectCreateRequest request) {
        final String normalizedTenant = normalize(tenantCode);
        if (projectRepository.existsByTenantCodeIgnoreCaseAndCodeIgnoreCase(normalizedTenant, request.code().trim())) {
            throw new NexraValidationException("Project code already exists for tenant.");
        }
        final OpsProjectEntity entity = new OpsProjectEntity();
        entity.setId(UUID.randomUUID().toString());
        entity.setTenantCode(normalizedTenant);
        entity.setCode(request.code().trim());
        entity.setName(request.name().trim());
        entity.setDescription(trimNullable(request.description()));
        entity.setOwnerUserId(request.ownerUserId().trim());
        entity.setStatus(operationsProperties.getDefaultProjectStatus());
        entity.setCrmDealId(trimNullable(request.crmDealId()));
        entity.setDepartmentCode(trimNullable(request.departmentCode()));
        entity.setStartDate(request.startDate());
        entity.setEndDate(request.endDate());
        return toModel(projectRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public OpsProject findById(final String tenantCode, final String projectId) {
        return toModel(load(tenantCode, projectId));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OpsProject> list(final String tenantCode, final int page, final int size) {
        final Page<OpsProjectEntity> result = projectRepository.findAllByTenantCodeIgnoreCaseOrderByUpdatedAtDescIdDesc(
            normalize(tenantCode),
            PageRequest.of(page, size)
        );
        final List<OpsProject> items = result.getContent().stream().map(this::toModel).toList();
        return new PageResponse<>(items, result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages(), result.hasNext(), result.hasPrevious());
    }

    @Override
    @Transactional
    public OpsProject createFromCrmDeal(final String tenantCode, final String dealId, final String ownerUserId) {
        final String normalizedTenant = normalize(tenantCode);
        return projectRepository.findByTenantCodeIgnoreCaseAndCrmDealId(normalizedTenant, dealId.trim())
            .map(this::toModel)
            .orElseGet(() -> createProjectFromDeal(tenantCode, dealId, ownerUserId));
    }

    @Override
    @Transactional
    public OpsProject createFromCrmDealIfAbsent(final String tenantCode, final String dealId, final String ownerUserId) {
        return createFromCrmDeal(tenantCode, dealId, ownerUserId);
    }

    private OpsProject createProjectFromDeal(final String tenantCode, final String dealId, final String ownerUserId) {
        final CrmDealEntity deal = crmDealRepository.findByIdAndTenantCodeIgnoreCase(dealId, normalize(tenantCode))
            .orElseThrow(() -> new NexraNotFoundException("CRM deal not found for id: " + dealId));
        final String prefix = operationsProperties.getDealProjectCodePrefix();
        final String code = prefix + deal.getId().substring(0, Math.min(8, deal.getId().length())).toUpperCase();
        return create(
            tenantCode,
            new OpsProjectCreateRequest(
                code,
                deal.getTitle(),
                "Auto-created from CRM deal " + deal.getId(),
                ownerUserId,
                deal.getId(),
                null,
                null,
                deal.getExpectedCloseDate()
            )
        );
    }

    private OpsProjectEntity load(final String tenantCode, final String projectId) {
        return projectRepository.findByIdAndTenantCodeIgnoreCase(projectId, normalize(tenantCode))
            .orElseThrow(() -> new NexraNotFoundException("Operations project not found for id: " + projectId));
    }

    private OpsProject toModel(final OpsProjectEntity entity) {
        return new OpsProject(
            entity.getId(),
            entity.getTenantCode(),
            entity.getCode(),
            entity.getName(),
            entity.getDescription(),
            entity.getOwnerUserId(),
            entity.getStatus(),
            entity.getCrmDealId(),
            entity.getDepartmentCode(),
            entity.getStartDate(),
            entity.getEndDate(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    private String normalize(final String value) {
        if (value == null || value.isBlank()) {
            throw new NexraValidationException("Tenant code is required.");
        }
        return value.trim();
    }

    private String trimNullable(final String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
