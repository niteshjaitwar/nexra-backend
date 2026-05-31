package com.nexra.hrms.nexra.common.workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.nexra.hrms.nexra.common.exception.NexraValidationException;
import com.nexra.hrms.nexra.common.workflow.entity.WorkflowInstanceEntity;
import com.nexra.hrms.nexra.common.workflow.entity.WorkflowStepHistoryEntity;
import com.nexra.hrms.nexra.common.workflow.repository.WorkflowInstanceRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class WorkflowEngineTest {

    @Autowired
    private WorkflowRuntime workflowRuntime;

    @Autowired
    private WorkflowInstanceRepository instanceRepository;

    @Test
    void multiStepWorkflowAdvancesThroughStepsToCompletion() {
        final var submission = workflowRuntime.submit(
            "ACME", "OPS", "operations-multi-approval", "OPS_APPROVAL_REQUESTED",
            "requester@acme.test", Map.of("referenceId", "ref-1"));

        WorkflowInstanceEntity instance = instanceRepository.findById(submission.workflowRef()).orElseThrow();
        assertThat(instance.getStatus()).isEqualTo("IN_PROGRESS");
        assertThat(instance.getCurrentStepName()).isEqualTo("MANAGER_REVIEW");
        assertThat(instance.getSlaDueAt()).isNotNull();

        workflowRuntime.advance("ACME", submission.workflowRef(), true, "manager@acme.test", "ok at step 1");
        instance = instanceRepository.findById(submission.workflowRef()).orElseThrow();
        assertThat(instance.getStatus()).isEqualTo("IN_PROGRESS");
        assertThat(instance.getCurrentStepName()).isEqualTo("FINAL_APPROVAL");

        workflowRuntime.advance("ACME", submission.workflowRef(), true, "director@acme.test", "final ok");
        instance = instanceRepository.findById(submission.workflowRef()).orElseThrow();
        assertThat(instance.getStatus()).isEqualTo("COMPLETED");
        assertThat(instance.getCompletedAt()).isNotNull();

        final List<WorkflowStepHistoryEntity> history = workflowRuntime.history("ACME", submission.workflowRef());
        assertThat(history).extracting(WorkflowStepHistoryEntity::getAction)
            .containsExactly("STARTED", "ADVANCED", "COMPLETED");
    }

    @Test
    void rejectingWorkflowTerminatesItAndBlocksFurtherAdvance() {
        final var submission = workflowRuntime.submit(
            "ACME", "OPS", "operations-multi-approval", "OPS_APPROVAL_REQUESTED",
            "requester@acme.test", Map.of("referenceId", "ref-2"));

        workflowRuntime.advance("ACME", submission.workflowRef(), false, "manager@acme.test", "not justified");
        final WorkflowInstanceEntity instance = instanceRepository.findById(submission.workflowRef()).orElseThrow();
        assertThat(instance.getStatus()).isEqualTo("REJECTED");

        assertThatThrownBy(() ->
            workflowRuntime.advance("ACME", submission.workflowRef(), true, "manager@acme.test", "late"))
            .isInstanceOf(NexraValidationException.class);
    }

    @Test
    void escalateOverdueFlagsBreachedSteps() {
        final var submission = workflowRuntime.submit(
            "ACME", "OPS", "operations-multi-approval", "OPS_APPROVAL_REQUESTED",
            "requester@acme.test", Map.of("referenceId", "ref-3"));

        final WorkflowInstanceEntity instance = instanceRepository.findById(submission.workflowRef()).orElseThrow();
        instance.setSlaDueAt(Instant.now().minus(10, ChronoUnit.MINUTES));
        instanceRepository.save(instance);

        final int escalated = workflowRuntime.escalateOverdue();
        assertThat(escalated).isGreaterThanOrEqualTo(1);

        final WorkflowInstanceEntity reloaded = instanceRepository.findById(submission.workflowRef()).orElseThrow();
        assertThat(reloaded.isEscalated()).isTrue();
        assertThat(reloaded.getEscalatedAt()).isNotNull();
    }
}
