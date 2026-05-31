package com.nexra.hrms.nexra.common.workflow;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexra.hrms.nexra.common.audit.AuditEventRecord;
import com.nexra.hrms.nexra.common.audit.AuditEventService;
import com.nexra.hrms.nexra.common.exception.NexraNotFoundException;
import com.nexra.hrms.nexra.common.exception.NexraValidationException;
import com.nexra.hrms.nexra.common.workflow.entity.WorkflowInstanceEntity;
import com.nexra.hrms.nexra.common.workflow.entity.WorkflowStepHistoryEntity;
import com.nexra.hrms.nexra.common.workflow.repository.WorkflowInstanceRepository;
import com.nexra.hrms.nexra.common.workflow.repository.WorkflowStepHistoryRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Multi-step workflow engine with per-step SLAs, escalation, and an append-only
 * step history. Modules submit a workflow which starts at the first configured
 * step; approvers advance it step by step until completion. A scheduled scan
 * flags steps that breach their SLA so they can be escalated.
 *
 * @author niteshjaitwar
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowRuntime {

    private static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String STATUS_REJECTED = "REJECTED";
    private static final String STEP_STATUS_PENDING = "PENDING";
    private static final String ACTION_STARTED = "STARTED";
    private static final String ACTION_ADVANCED = "ADVANCED";
    private static final String ACTION_REJECTED = "REJECTED";
    private static final String ACTION_COMPLETED = "COMPLETED";
    private static final String ACTION_ESCALATED = "ESCALATED";

    private final WorkflowInstanceRepository instanceRepository;
    private final WorkflowStepHistoryRepository stepHistoryRepository;
    private final ObjectMapper objectMapper;
    private final WorkflowProperties workflowProperties;
    private final AuditEventService auditEventService;

    /**
     * Starts a workflow instance for the given module trigger. When the module
     * has configured steps the instance begins IN_PROGRESS at step 0 with an SLA
     * deadline; otherwise it is recorded as an immediately accepted single event.
     *
     * @return submission result with the workflow reference and status.
     */
    @Transactional
    public WorkflowSubmissionResult submit(
        final String tenantCode,
        final String productCode,
        final String moduleKey,
        final String triggerEvent,
        final String actorEmail,
        final Map<String, Object> payload
    ) {
        final WorkflowInstanceEntity instance = new WorkflowInstanceEntity();
        instance.setId(UUID.randomUUID().toString());
        instance.setTenantCode(normalize(tenantCode));
        instance.setProductCode(productCode);
        instance.setModuleKey(moduleKey);
        instance.setTriggerEvent(triggerEvent);
        instance.setActorEmail(actorEmail);
        instance.setPayloadJson(writeJson(payload));

        final WorkflowProperties.ModuleWorkflow definition = workflowProperties.resolveModule(moduleKey);
        if (definition == null || definition.getSteps().isEmpty()) {
            instance.setStatus(workflowProperties.getSubmissionStatus());
            instance.setStepStatus(workflowProperties.getSubmissionStatus());
            instance.setCompletedAt(Instant.now());
            instance.setResultJson(writeJson(Map.of(
                "workflowRef", instance.getId(),
                "triggerEvent", triggerEvent,
                "status", workflowProperties.getSubmissionStatus()
            )));
            final WorkflowInstanceEntity savedSingle = instanceRepository.save(instance);
            recordHistory(savedSingle, ACTION_COMPLETED, actorEmail, "Single-event workflow recorded.");
            return new WorkflowSubmissionResult(savedSingle.getId(), savedSingle.getStatus(), savedSingle.getCreatedAt().toString());
        }

        final WorkflowProperties.Step firstStep = definition.getSteps().get(0);
        instance.setStatus(STATUS_IN_PROGRESS);
        instance.setCurrentStepIndex(0);
        instance.setCurrentStepName(firstStep.getName());
        instance.setStepStatus(STEP_STATUS_PENDING);
        instance.setSlaDueAt(Instant.now().plus(firstStep.getSlaMinutes(), ChronoUnit.MINUTES));
        instance.setResultJson(writeJson(Map.of(
            "workflowRef", instance.getId(),
            "triggerEvent", triggerEvent,
            "status", STATUS_IN_PROGRESS,
            "currentStep", firstStep.getName()
        )));
        final WorkflowInstanceEntity saved = instanceRepository.save(instance);
        recordHistory(saved, ACTION_STARTED, actorEmail, "Workflow started at step " + firstStep.getName());
        return new WorkflowSubmissionResult(saved.getId(), saved.getStatus(), saved.getCreatedAt().toString());
    }

    /**
     * Advances a workflow instance. An APPROVE moves to the next step or completes
     * the workflow; a REJECT terminates it. Rejects the call when the instance is
     * already terminal.
     *
     * @param tenantCode tenant owning the instance (enforces isolation).
     * @param instanceId workflow instance id.
     * @param approve    true to approve/advance, false to reject.
     * @param actorEmail acting approver email.
     * @param notes      optional decision notes.
     * @return updated instance.
     */
    @Transactional
    public WorkflowInstanceEntity advance(
        final String tenantCode,
        final String instanceId,
        final boolean approve,
        final String actorEmail,
        final String notes
    ) {
        final String normalizedTenant = normalize(tenantCode);
        final WorkflowInstanceEntity instance = instanceRepository
            .findByIdAndTenantCodeIgnoreCase(instanceId, normalizedTenant)
            .orElseThrow(() -> new NexraNotFoundException("Workflow instance not found for id: " + instanceId));

        if (!STATUS_IN_PROGRESS.equalsIgnoreCase(instance.getStatus())) {
            throw new NexraValidationException("Workflow instance is not in progress and cannot be advanced.");
        }

        final WorkflowProperties.ModuleWorkflow definition = workflowProperties.resolveModule(instance.getModuleKey());
        final List<WorkflowProperties.Step> steps = definition == null ? List.of() : definition.getSteps();

        if (!approve) {
            instance.setStatus(STATUS_REJECTED);
            instance.setStepStatus(STATUS_REJECTED);
            instance.setCompletedAt(Instant.now());
            final WorkflowInstanceEntity saved = instanceRepository.save(instance);
            recordHistory(saved, ACTION_REJECTED, actorEmail, notes);
            audit(saved, "WORKFLOW_REJECTED", actorEmail);
            return saved;
        }

        final int nextIndex = instance.getCurrentStepIndex() + 1;
        if (nextIndex >= steps.size()) {
            instance.setStatus(STATUS_COMPLETED);
            instance.setStepStatus(STATUS_COMPLETED);
            instance.setSlaDueAt(null);
            instance.setCompletedAt(Instant.now());
            final WorkflowInstanceEntity saved = instanceRepository.save(instance);
            recordHistory(saved, ACTION_COMPLETED, actorEmail, notes);
            audit(saved, "WORKFLOW_COMPLETED", actorEmail);
            return saved;
        }

        final WorkflowProperties.Step nextStep = steps.get(nextIndex);
        instance.setCurrentStepIndex(nextIndex);
        instance.setCurrentStepName(nextStep.getName());
        instance.setStepStatus(STEP_STATUS_PENDING);
        instance.setSlaDueAt(Instant.now().plus(nextStep.getSlaMinutes(), ChronoUnit.MINUTES));
        instance.setEscalated(false);
        instance.setEscalatedAt(null);
        final WorkflowInstanceEntity saved = instanceRepository.save(instance);
        recordHistory(saved, ACTION_ADVANCED, actorEmail, "Advanced to step " + nextStep.getName());
        audit(saved, "WORKFLOW_ADVANCED", actorEmail);
        return saved;
    }

    /**
     * Returns the step history for an instance, tenant-scoped.
     *
     * @param tenantCode tenant owning the instance.
     * @param instanceId workflow instance id.
     * @return ordered step history.
     */
    @Transactional(readOnly = true)
    public List<WorkflowStepHistoryEntity> history(final String tenantCode, final String instanceId) {
        instanceRepository.findByIdAndTenantCodeIgnoreCase(instanceId, normalize(tenantCode))
            .orElseThrow(() -> new NexraNotFoundException("Workflow instance not found for id: " + instanceId));
        return stepHistoryRepository.findAllByInstanceIdOrderByStepIndexAscCreatedAtAsc(instanceId);
    }

    /**
     * Flags in-progress instances whose current step has breached its SLA. Each
     * flagged instance records an ESCALATED history entry and an audit event.
     *
     * @return number of instances escalated in this scan.
     */
    @Transactional
    public int escalateOverdue() {
        final List<WorkflowInstanceEntity> overdue = instanceRepository
            .findAllByStatusAndEscalatedFalseAndSlaDueAtBefore(STATUS_IN_PROGRESS, Instant.now());
        for (final WorkflowInstanceEntity instance : overdue) {
            instance.setEscalated(true);
            instance.setEscalatedAt(Instant.now());
            final WorkflowInstanceEntity saved = instanceRepository.save(instance);
            recordHistory(saved, ACTION_ESCALATED, "system",
                "SLA breached at step " + saved.getCurrentStepName());
            audit(saved, "WORKFLOW_ESCALATED", "system");
        }
        if (!overdue.isEmpty()) {
            log.info("WorkflowRuntime - escalateOverdue - escalated {} overdue workflow instances", overdue.size());
        }
        return overdue.size();
    }

    private void recordHistory(
        final WorkflowInstanceEntity instance,
        final String action,
        final String actorEmail,
        final String notes
    ) {
        final WorkflowStepHistoryEntity history = new WorkflowStepHistoryEntity();
        history.setId(UUID.randomUUID().toString());
        history.setInstanceId(instance.getId());
        history.setTenantCode(instance.getTenantCode());
        history.setStepIndex(instance.getCurrentStepIndex());
        history.setStepName(instance.getCurrentStepName() == null ? action : instance.getCurrentStepName());
        history.setAction(action);
        history.setActorEmail(actorEmail);
        history.setNotes(notes == null || notes.isBlank() ? null : notes.trim());
        stepHistoryRepository.save(history);
    }

    private void audit(final WorkflowInstanceEntity instance, final String action, final String actorEmail) {
        auditEventService.record(AuditEventRecord
            .of(instance.getTenantCode(), "WORKFLOW", action, "SUCCESS")
            .withActor(actorEmail, null)
            .withTarget("WORKFLOW_INSTANCE", instance.getId()));
    }

    private String writeJson(final Map<String, Object> payload) {
        if (payload == null || payload.isEmpty()) {
            return "{}";
        }
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            return "{}";
        }
    }

    private String normalize(final String tenantCode) {
        return tenantCode == null ? "" : tenantCode.trim().toUpperCase(Locale.ROOT);
    }

    public record WorkflowSubmissionResult(String workflowRef, String status, String receivedAt) {
    }
}
