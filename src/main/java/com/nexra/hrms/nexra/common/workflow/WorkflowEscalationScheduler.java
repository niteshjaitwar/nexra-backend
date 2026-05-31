package com.nexra.hrms.nexra.common.workflow;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Periodically scans for workflow steps that have breached their SLA and
 * escalates them. The scan interval is externally configurable.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WorkflowEscalationScheduler {

    private final WorkflowRuntime workflowRuntime;

    @Scheduled(
        initialDelayString = "#{${nexra.workflow.escalation-scan-seconds:300} * 1000}",
        fixedDelayString = "#{${nexra.workflow.escalation-scan-seconds:300} * 1000}"
    )
    public void scanForEscalations() {
        try {
            workflowRuntime.escalateOverdue();
        } catch (Exception ex) {
            log.error("WorkflowEscalationScheduler - scanForEscalations failed - {}", ex.getMessage(), ex);
        }
    }
}
