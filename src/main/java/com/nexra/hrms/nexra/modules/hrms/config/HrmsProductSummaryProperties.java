package com.nexra.hrms.nexra.modules.hrms.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "nexra.hrms.product-summary")
public class HrmsProductSummaryProperties {

    private int attendanceLookbackDays = 30;
    private Map<String, ModuleSummaryRule> modules = new HashMap<>();

    public int getAttendanceLookbackDays() {
        return attendanceLookbackDays;
    }

    public void setAttendanceLookbackDays(final int attendanceLookbackDays) {
        this.attendanceLookbackDays = attendanceLookbackDays;
    }

    public Map<String, ModuleSummaryRule> getModules() {
        return modules;
    }

    public void setModules(final Map<String, ModuleSummaryRule> modules) {
        this.modules = modules;
    }

    public static class ModuleSummaryRule {
        private SummaryMetric queueMetric = SummaryMetric.TOTAL_RECORDS;
        private SummaryMetric pendingMetric = SummaryMetric.QUEUE_FRACTION;
        private List<String> pendingStatuses = List.of();
        private String pendingStatus;
        private String pendingStage;

        public SummaryMetric getQueueMetric() {
            return queueMetric;
        }

        public void setQueueMetric(final SummaryMetric queueMetric) {
            this.queueMetric = queueMetric;
        }

        public SummaryMetric getPendingMetric() {
            return pendingMetric;
        }

        public void setPendingMetric(final SummaryMetric pendingMetric) {
            this.pendingMetric = pendingMetric;
        }

        public List<String> getPendingStatuses() {
            return pendingStatuses;
        }

        public void setPendingStatuses(final List<String> pendingStatuses) {
            this.pendingStatuses = pendingStatuses;
        }

        public String getPendingStatus() {
            return pendingStatus;
        }

        public void setPendingStatus(final String pendingStatus) {
            this.pendingStatus = pendingStatus;
        }

        public String getPendingStage() {
            return pendingStage;
        }

        public void setPendingStage(final String pendingStage) {
            this.pendingStage = pendingStage;
        }
    }

    public enum SummaryMetric {
        ACTIVE_EMPLOYEES,
        TOTAL_RECORDS,
        ATTENDANCE_WINDOW,
        STATUS_IN,
        STATUS_EQUALS,
        STAGE_EQUALS,
        OPEN_CHECKOUT_IN_WINDOW,
        QUEUE_FRACTION
    }
}
