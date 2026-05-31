package com.nexra.hrms.nexra.common.workflow;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Externalized workflow engine configuration. Each module key maps to an ordered
 * list of steps; every step carries its own SLA (in minutes) and an escalation
 * role so overdue work can be routed without code changes.
 */
@ConfigurationProperties(prefix = "nexra.workflow")
public class WorkflowProperties {

    private String submissionStatus = "ACCEPTED";
    private String hrmsSubmitTrigger = "HRMS_WORKFLOW_SUBMIT";
    private int escalationScanSeconds = 300;
    private Map<String, ModuleWorkflow> modules = defaultModules();

    private static Map<String, ModuleWorkflow> defaultModules() {
        final Map<String, ModuleWorkflow> modules = new LinkedHashMap<>();
        modules.put("operations-approvals", singleStep("MANAGER_REVIEW", 480, "OPS_MANAGER"));
        modules.put("operations-multi-approval", twoStep());
        return modules;
    }

    private static ModuleWorkflow singleStep(final String name, final int slaMinutes, final String escalationRole) {
        final ModuleWorkflow workflow = new ModuleWorkflow();
        final Step step = new Step();
        step.setName(name);
        step.setSlaMinutes(slaMinutes);
        step.setEscalationRole(escalationRole);
        workflow.setSteps(new ArrayList<>(List.of(step)));
        return workflow;
    }

    private static ModuleWorkflow twoStep() {
        final ModuleWorkflow workflow = new ModuleWorkflow();
        final Step review = new Step();
        review.setName("MANAGER_REVIEW");
        review.setSlaMinutes(480);
        review.setEscalationRole("OPS_MANAGER");
        final Step approve = new Step();
        approve.setName("FINAL_APPROVAL");
        approve.setSlaMinutes(960);
        approve.setEscalationRole("TENANT_ADMIN");
        workflow.setSteps(new ArrayList<>(List.of(review, approve)));
        return workflow;
    }

    public ModuleWorkflow resolveModule(final String moduleKey) {
        if (moduleKey == null) {
            return null;
        }
        final String key = moduleKey.trim().toLowerCase(Locale.ROOT);
        return modules.get(key);
    }

    public String getSubmissionStatus() {
        return submissionStatus;
    }

    public void setSubmissionStatus(final String submissionStatus) {
        this.submissionStatus = submissionStatus;
    }

    public String getHrmsSubmitTrigger() {
        return hrmsSubmitTrigger;
    }

    public void setHrmsSubmitTrigger(final String hrmsSubmitTrigger) {
        this.hrmsSubmitTrigger = hrmsSubmitTrigger;
    }

    public int getEscalationScanSeconds() {
        return escalationScanSeconds;
    }

    public void setEscalationScanSeconds(final int escalationScanSeconds) {
        this.escalationScanSeconds = escalationScanSeconds;
    }

    public Map<String, ModuleWorkflow> getModules() {
        return modules;
    }

    public void setModules(final Map<String, ModuleWorkflow> modules) {
        this.modules = modules;
    }

    /**
     * An ordered set of approval steps for a module workflow.
     */
    public static class ModuleWorkflow {

        private List<Step> steps = new ArrayList<>();

        public List<Step> getSteps() {
            return steps;
        }

        public void setSteps(final List<Step> steps) {
            this.steps = steps;
        }
    }

    /**
     * A single workflow step with its SLA and escalation target.
     */
    public static class Step {

        private String name;
        private int slaMinutes = 1440;
        private String escalationRole = "TENANT_ADMIN";

        public String getName() {
            return name;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public int getSlaMinutes() {
            return slaMinutes;
        }

        public void setSlaMinutes(final int slaMinutes) {
            this.slaMinutes = slaMinutes;
        }

        public String getEscalationRole() {
            return escalationRole;
        }

        public void setEscalationRole(final String escalationRole) {
            this.escalationRole = escalationRole;
        }
    }
}
