package com.nexra.hrms.nexra.common.workflow;

import com.nexra.hrms.nexra.common.api.PageResponse;
import com.nexra.hrms.nexra.common.exception.NexraNotFoundException;
import com.nexra.hrms.nexra.common.exception.NexraValidationException;
import com.nexra.hrms.nexra.common.workflow.entity.WorkflowInstanceEntity;
import com.nexra.hrms.nexra.common.workflow.entity.WorkflowStepHistoryEntity;
import com.nexra.hrms.nexra.common.workflow.model.WorkflowInstance;
import com.nexra.hrms.nexra.common.workflow.model.WorkflowStepHistory;
import com.nexra.hrms.nexra.common.workflow.repository.WorkflowInstanceRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Read-only query surface for workflow instances and step history. Mutations
 * remain on {@link WorkflowRuntime}; this service exposes tenant-scoped views
 * for operations and audit dashboards.
 */
@Service
@RequiredArgsConstructor
public class WorkflowQueryService {

    private final WorkflowInstanceRepository instanceRepository;
    private final WorkflowRuntime workflowRuntime;

    @Transactional(readOnly = true)
    public PageResponse<WorkflowInstance> listInstances(final String tenantCode, final int page, final int size) {
        final String normalizedTenant = requireTenant(tenantCode);
        if (page < 0 || size <= 0 || size > 100) {
            throw new NexraValidationException("Page size must be between 1 and 100.");
        }
        final Page<WorkflowInstanceEntity> result = instanceRepository
            .findAllByTenantCodeIgnoreCaseOrderByCreatedAtDescIdDesc(normalizedTenant, PageRequest.of(page, size));
        final List<WorkflowInstance> items = result.getContent().stream().map(this::toModel).toList();
        return new PageResponse<>(items, result.getNumber(), result.getSize(), result.getTotalElements(),
            result.getTotalPages(), result.hasNext(), result.hasPrevious());
    }

    @Transactional(readOnly = true)
    public WorkflowInstance getInstance(final String tenantCode, final String instanceId) {
        return toModel(loadInstance(requireTenant(tenantCode), instanceId));
    }

    @Transactional(readOnly = true)
    public List<WorkflowStepHistory> getStepHistory(final String tenantCode, final String instanceId) {
        loadInstance(requireTenant(tenantCode), instanceId);
        return workflowRuntime.history(requireTenant(tenantCode), instanceId).stream()
            .map(this::toHistoryModel)
            .toList();
    }

    private WorkflowInstanceEntity loadInstance(final String tenantCode, final String instanceId) {
        return instanceRepository.findByIdAndTenantCodeIgnoreCase(instanceId, tenantCode)
            .orElseThrow(() -> new NexraNotFoundException("Workflow instance not found for id: " + instanceId));
    }

    private WorkflowInstance toModel(final WorkflowInstanceEntity entity) {
        return new WorkflowInstance(
            entity.getId(),
            entity.getTenantCode(),
            entity.getProductCode(),
            entity.getModuleKey(),
            entity.getTriggerEvent(),
            entity.getStatus(),
            entity.getActorEmail(),
            entity.getCurrentStepIndex(),
            entity.getCurrentStepName(),
            entity.getStepStatus(),
            entity.getSlaDueAt(),
            entity.isEscalated(),
            entity.getEscalatedAt(),
            entity.getCompletedAt(),
            entity.getCreatedAt()
        );
    }

    private WorkflowStepHistory toHistoryModel(final WorkflowStepHistoryEntity entity) {
        return new WorkflowStepHistory(
            entity.getId(),
            entity.getInstanceId(),
            entity.getStepIndex(),
            entity.getStepName(),
            entity.getAction(),
            entity.getActorEmail(),
            entity.getNotes(),
            entity.getCreatedAt()
        );
    }

    private String requireTenant(final String tenantCode) {
        if (tenantCode == null || tenantCode.isBlank()) {
            throw new NexraValidationException("Tenant code is required.");
        }
        return tenantCode.trim().toUpperCase(java.util.Locale.ROOT);
    }
}
