package com.nexra.hrms.nexra.modules.operations.service.impl;

import com.nexra.hrms.nexra.common.api.PageResponse;
import com.nexra.hrms.nexra.common.audit.AuditEventRecord;
import com.nexra.hrms.nexra.common.audit.AuditEventService;
import com.nexra.hrms.nexra.common.exception.NexraNotFoundException;
import com.nexra.hrms.nexra.common.exception.NexraValidationException;
import com.nexra.hrms.nexra.modules.operations.config.OperationsProperties;
import com.nexra.hrms.nexra.modules.operations.dto.OpsTaskCreateRequest;
import com.nexra.hrms.nexra.modules.operations.dto.OpsTaskStatusUpdateRequest;
import com.nexra.hrms.nexra.modules.operations.entity.OpsTaskEntity;
import com.nexra.hrms.nexra.modules.operations.model.OpsTask;
import com.nexra.hrms.nexra.modules.operations.repository.OpsProjectRepository;
import com.nexra.hrms.nexra.modules.operations.repository.OpsTaskRepository;
import com.nexra.hrms.nexra.modules.operations.service.OpsTaskService;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OpsTaskServiceImpl implements OpsTaskService {

    private static final String AUDIT_MODULE = "OPERATIONS";
    private static final Set<String> ALLOWED_STATUSES = Set.of("OPEN", "IN_PROGRESS", "DONE", "CANCELLED");

    private final OpsTaskRepository taskRepository;
    private final OpsProjectRepository projectRepository;
    private final OperationsProperties operationsProperties;
    private final AuditEventService auditEventService;

    @Override
    @Transactional
    public OpsTask create(final String tenantCode, final String actorEmail, final OpsTaskCreateRequest request) {
        final String normalizedTenant = tenantCode.trim().toUpperCase(Locale.ROOT);
        projectRepository.findByIdAndTenantCodeIgnoreCase(request.projectId().trim(), normalizedTenant)
            .orElseThrow(() -> new NexraNotFoundException("Operations project not found for id: " + request.projectId()));
        final OpsTaskEntity entity = new OpsTaskEntity();
        entity.setId(UUID.randomUUID().toString());
        entity.setTenantCode(normalizedTenant);
        entity.setProjectId(request.projectId().trim());
        entity.setParentTaskId(trimNullable(request.parentTaskId()));
        entity.setTitle(request.title().trim());
        entity.setDescription(trimNullable(request.description()));
        entity.setAssigneeUserId(trimNullable(request.assigneeUserId()));
        entity.setStatus(operationsProperties.getDefaultTaskStatus());
        entity.setPriority(request.priority() == null || request.priority().isBlank()
            ? operationsProperties.getDefaultTaskPriority()
            : request.priority().trim().toUpperCase(Locale.ROOT));
        entity.setDueDate(request.dueDate());
        entity.setEstimateHours(request.estimateHours());
        final OpsTaskEntity saved = taskRepository.save(entity);
        auditEventService.record(AuditEventRecord
            .of(normalizedTenant, AUDIT_MODULE, "OPS_TASK_CREATED", "SUCCESS")
            .withActor(actorEmail, null)
            .withTarget("OPS_TASK", saved.getId()));
        return toModel(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OpsTask> listByProject(final String tenantCode, final String projectId, final int page, final int size) {
        final String normalizedTenant = tenantCode.trim().toUpperCase(Locale.ROOT);
        final Page<OpsTaskEntity> result = taskRepository.findAllByTenantCodeIgnoreCaseAndProjectIdOrderByUpdatedAtDescIdDesc(
            normalizedTenant,
            projectId.trim(),
            PageRequest.of(page, size)
        );
        final List<OpsTask> items = result.getContent().stream().map(this::toModel).toList();
        return new PageResponse<>(items, result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages(), result.hasNext(), result.hasPrevious());
    }

    @Override
    @Transactional
    public OpsTask updateStatus(
        final String tenantCode,
        final String actorEmail,
        final String taskId,
        final OpsTaskStatusUpdateRequest request
    ) {
        final String normalizedTenant = tenantCode.trim().toUpperCase(Locale.ROOT);
        final String status = request.status().trim().toUpperCase(Locale.ROOT);
        if (!ALLOWED_STATUSES.contains(status)) {
            throw new NexraValidationException("Unsupported task status: " + status);
        }
        final OpsTaskEntity entity = taskRepository.findByIdAndTenantCodeIgnoreCase(taskId.trim(), normalizedTenant)
            .orElseThrow(() -> new NexraNotFoundException("Operations task not found for id: " + taskId));
        entity.setStatus(status);
        final OpsTaskEntity saved = taskRepository.save(entity);
        auditEventService.record(AuditEventRecord
            .of(normalizedTenant, AUDIT_MODULE, "OPS_TASK_STATUS_UPDATED", "SUCCESS")
            .withActor(actorEmail, null)
            .withTarget("OPS_TASK", saved.getId())
            .withDetail("{\"status\":\"" + status + "\"}"));
        return toModel(saved);
    }

    private OpsTask toModel(final OpsTaskEntity entity) {
        return new OpsTask(
            entity.getId(),
            entity.getProjectId(),
            entity.getTitle(),
            entity.getStatus(),
            entity.getPriority(),
            entity.getAssigneeUserId() == null ? "" : entity.getAssigneeUserId(),
            entity.getDueDate(),
            entity.getEstimateHours()
        );
    }

    private String trimNullable(final String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
