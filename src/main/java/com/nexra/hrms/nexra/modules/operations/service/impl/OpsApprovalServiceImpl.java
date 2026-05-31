package com.nexra.hrms.nexra.modules.operations.service.impl;

import com.nexra.hrms.nexra.common.api.PageResponse;
import com.nexra.hrms.nexra.common.audit.AuditEventRecord;
import com.nexra.hrms.nexra.common.audit.AuditEventService;
import com.nexra.hrms.nexra.common.exception.NexraForbiddenException;
import com.nexra.hrms.nexra.common.exception.NexraNotFoundException;
import com.nexra.hrms.nexra.common.exception.NexraValidationException;
import com.nexra.hrms.nexra.common.workflow.WorkflowRuntime;
import com.nexra.hrms.nexra.modules.operations.config.OperationsProperties;
import com.nexra.hrms.nexra.modules.operations.dto.OpsApprovalCreateRequest;
import com.nexra.hrms.nexra.modules.operations.dto.OpsApprovalDecisionRequest;
import com.nexra.hrms.nexra.modules.operations.entity.OpsApprovalRequestEntity;
import com.nexra.hrms.nexra.modules.operations.model.OpsApprovalRequest;
import com.nexra.hrms.nexra.modules.operations.repository.OpsApprovalRequestRepository;
import com.nexra.hrms.nexra.modules.operations.service.OpsApprovalService;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Manages Operations approval requests including state-validated approve/reject
 * decisions. Every mutation emits an audit event and records a workflow instance
 * so the decision is traceable for compliance review.
 */
@Service
@RequiredArgsConstructor
public class OpsApprovalServiceImpl implements OpsApprovalService {

    private static final String AUDIT_MODULE = "OPERATIONS";
    private static final String PRODUCT_CODE = "OPS";
    private static final String MODULE_KEY = "operations-approvals";

    private final OpsApprovalRequestRepository approvalRepository;
    private final OperationsProperties operationsProperties;
    private final AuditEventService auditEventService;
    private final WorkflowRuntime workflowRuntime;

    @Override
    @Transactional
    public OpsApprovalRequest create(final String tenantCode, final String actorEmail, final OpsApprovalCreateRequest request) {
        final String normalizedTenant = normalize(tenantCode);
        final OpsApprovalRequestEntity entity = new OpsApprovalRequestEntity();
        entity.setId(UUID.randomUUID().toString());
        entity.setTenantCode(normalizedTenant);
        entity.setReferenceType(request.referenceType().trim().toUpperCase(Locale.ROOT));
        entity.setReferenceId(request.referenceId().trim());
        entity.setRequestedByUserId(request.requestedByUserId().trim());
        entity.setApproverUserId(trimNullable(request.approverUserId()));
        entity.setStatus(operationsProperties.getDefaultApprovalStatus());
        entity.setNotes(trimNullable(request.notes()));

        final WorkflowRuntime.WorkflowSubmissionResult workflow = workflowRuntime.submit(
            normalizedTenant, PRODUCT_CODE, MODULE_KEY, "OPS_APPROVAL_REQUESTED", actorEmail,
            Map.of("referenceType", entity.getReferenceType(), "referenceId", entity.getReferenceId()));
        entity.setWorkflowInstanceId(workflow.workflowRef());
        final OpsApprovalRequestEntity saved = approvalRepository.save(entity);

        auditEventService.record(AuditEventRecord
            .of(normalizedTenant, AUDIT_MODULE, "OPS_APPROVAL_REQUESTED", "SUCCESS")
            .withActor(actorEmail, null)
            .withTarget("OPS_APPROVAL_REQUEST", saved.getId()));
        return toModel(saved);
    }

    @Override
    @Transactional
    public OpsApprovalRequest decide(
        final String tenantCode,
        final String actorEmail,
        final String actorUserId,
        final Set<String> actorRoles,
        final String approvalId,
        final OpsApprovalDecisionRequest request
    ) {
        final String normalizedTenant = normalize(tenantCode);
        final OpsApprovalRequestEntity entity = approvalRepository
            .findByIdAndTenantCodeIgnoreCase(approvalId.trim(), normalizedTenant)
            .orElseThrow(() -> new NexraNotFoundException("Operations approval request not found for id: " + approvalId));

        if (!operationsProperties.getDefaultApprovalStatus().equalsIgnoreCase(entity.getStatus())) {
            auditEventService.record(AuditEventRecord
                .of(normalizedTenant, AUDIT_MODULE, "OPS_APPROVAL_DECISION", "FAILURE")
                .withActor(actorEmail, null)
                .withTarget("OPS_APPROVAL_REQUEST", entity.getId())
                .withDetail("Approval is already in terminal state: " + entity.getStatus()));
            throw new NexraValidationException("Approval request is not pending and cannot be decided again.");
        }

        assertCanDecide(entity, actorUserId, actorRoles);

        final boolean approved = "APPROVE".equalsIgnoreCase(request.decision().trim());
        final String newStatus = approved
            ? operationsProperties.getApprovedStatus()
            : operationsProperties.getRejectedStatus();
        entity.setStatus(newStatus);
        entity.setApproverUserId(actorUserId.trim());
        entity.setNotes(trimNullable(request.notes()));
        entity.setDecidedAt(Instant.now());
        final OpsApprovalRequestEntity saved = approvalRepository.save(entity);

        final String action = approved ? "OPS_APPROVAL_APPROVED" : "OPS_APPROVAL_REJECTED";
        auditEventService.record(AuditEventRecord
            .of(normalizedTenant, AUDIT_MODULE, action, "SUCCESS")
            .withActor(actorEmail, null)
            .withTarget("OPS_APPROVAL_REQUEST", saved.getId()));
        if (saved.getWorkflowInstanceId() != null) {
            workflowRuntime.advance(normalizedTenant, saved.getWorkflowInstanceId(), approved, actorEmail,
                trimNullable(request.notes()));
        }
        return toModel(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OpsApprovalRequest> list(final String tenantCode, final int page, final int size) {
        final Page<OpsApprovalRequestEntity> result = approvalRepository.findAllByTenantCodeIgnoreCaseOrderByUpdatedAtDescIdDesc(
            normalize(tenantCode),
            PageRequest.of(page, size)
        );
        final List<OpsApprovalRequest> items = result.getContent().stream().map(this::toModel).toList();
        return new PageResponse<>(items, result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages(), result.hasNext(), result.hasPrevious());
    }

    private void assertCanDecide(
        final OpsApprovalRequestEntity entity,
        final String actorUserId,
        final Set<String> actorRoles
    ) {
        final String assigned = trimNullable(entity.getApproverUserId());
        if (assigned == null) {
            return;
        }
        if (assigned.equalsIgnoreCase(actorUserId.trim())) {
            return;
        }
        if (hasAnyRole(actorRoles, "ROLE_PLATFORM_ADMIN", "ROLE_TENANT_ADMIN", "ROLE_HR_ADMIN", "ROLE_OPS_MANAGER")) {
            return;
        }
        throw new NexraForbiddenException("Only the assigned approver or an operations manager can decide this approval.");
    }

    private boolean hasAnyRole(final Set<String> roles, final String... expected) {
        if (roles == null || roles.isEmpty()) {
            return false;
        }
        for (final String role : expected) {
            if (roles.contains(role)) {
                return true;
            }
        }
        return false;
    }

    private OpsApprovalRequest toModel(final OpsApprovalRequestEntity entity) {
        return new OpsApprovalRequest(
            entity.getId(),
            entity.getReferenceType(),
            entity.getReferenceId(),
            entity.getStatus(),
            entity.getRequestedByUserId(),
            entity.getApproverUserId() == null ? "" : entity.getApproverUserId()
        );
    }

    private String normalize(final String tenantCode) {
        return tenantCode == null ? "" : tenantCode.trim().toUpperCase(Locale.ROOT);
    }

    private String trimNullable(final String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
