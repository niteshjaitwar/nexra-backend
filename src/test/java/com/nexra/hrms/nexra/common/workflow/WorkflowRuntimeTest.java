package com.nexra.hrms.nexra.common.workflow;

import static org.assertj.core.api.Assertions.assertThat;

import com.nexra.hrms.nexra.common.workflow.repository.WorkflowInstanceRepository;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class WorkflowRuntimeTest {

    @Autowired
    private WorkflowRuntime workflowRuntime;

    @Autowired
    private WorkflowInstanceRepository workflowInstanceRepository;

    @Test
    void submitPersistsWorkflowInstance() {
        final WorkflowRuntime.WorkflowSubmissionResult result = workflowRuntime.submit(
            "ACME",
            "HRMS",
            "leave",
            "LEAVE_SUBMIT",
            "user@acme.test",
            Map.of("requestId", "lr-1")
        );
        assertThat(result.workflowRef()).isNotBlank();
        assertThat(workflowInstanceRepository.findById(result.workflowRef())).isPresent();
    }
}
