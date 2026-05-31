package com.nexra.hrms.nexra.common.workflow;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexra.hrms.nexra.modules.crm.entity.CrmWorkflowRuleEntity;
import com.nexra.hrms.nexra.modules.crm.repository.CrmWorkflowRuleRepository;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CrmWorkflowExecutor {

    private final CrmWorkflowRuleRepository workflowRuleRepository;
    private final WorkflowRuntime workflowRuntime;
    private final ObjectMapper objectMapper;

    public void onEvent(
        final String tenantCode,
        final String moduleKey,
        final String triggerEvent,
        final String actorEmail,
        final Map<String, Object> payload
    ) {
        final List<CrmWorkflowRuleEntity> rules = workflowRuleRepository
            .findAllByTenantCodeIgnoreCaseAndModuleKeyIgnoreCaseAndActiveTrueOrderByPriorityAscNameAsc(tenantCode, moduleKey);
        for (final CrmWorkflowRuleEntity rule : rules) {
            if (!rule.getTriggerEvent().equalsIgnoreCase(triggerEvent)) {
                continue;
            }
            workflowRuntime.submit(
                tenantCode,
                "CRM",
                moduleKey,
                triggerEvent,
                actorEmail,
                Map.of(
                    "ruleId", rule.getId(),
                    "ruleName", rule.getName(),
                    "criteria", readJson(rule.getCriteriaJson()),
                    "actions", readJson(rule.getActionsJson()),
                    "eventPayload", payload == null ? Map.of() : payload
                )
            );
        }
    }

    private Map<String, Object> readJson(final String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (Exception ex) {
            return Map.of("raw", json);
        }
    }
}
