package com.nexra.hrms.nexra.modules.operations.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "nexra.operations")
public class OperationsProperties {

    private boolean enabled = true;
    private int maxPageSize = 100;
    private String dealProjectCodePrefix = "DEAL-";
    private String defaultProjectStatus = "ACTIVE";
    private String defaultTaskStatus = "OPEN";
    private String defaultTaskPriority = "MEDIUM";
    private String defaultApprovalStatus = "PENDING";
    private String approvedStatus = "APPROVED";
    private String rejectedStatus = "REJECTED";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public int getMaxPageSize() {
        return maxPageSize;
    }

    public void setMaxPageSize(final int maxPageSize) {
        this.maxPageSize = maxPageSize;
    }

    public String getDealProjectCodePrefix() {
        return dealProjectCodePrefix;
    }

    public void setDealProjectCodePrefix(final String dealProjectCodePrefix) {
        this.dealProjectCodePrefix = dealProjectCodePrefix;
    }

    public String getDefaultProjectStatus() {
        return defaultProjectStatus;
    }

    public void setDefaultProjectStatus(final String defaultProjectStatus) {
        this.defaultProjectStatus = defaultProjectStatus;
    }

    public String getDefaultTaskStatus() {
        return defaultTaskStatus;
    }

    public void setDefaultTaskStatus(final String defaultTaskStatus) {
        this.defaultTaskStatus = defaultTaskStatus;
    }

    public String getDefaultTaskPriority() {
        return defaultTaskPriority;
    }

    public void setDefaultTaskPriority(final String defaultTaskPriority) {
        this.defaultTaskPriority = defaultTaskPriority;
    }

    public String getDefaultApprovalStatus() {
        return defaultApprovalStatus;
    }

    public void setDefaultApprovalStatus(final String defaultApprovalStatus) {
        this.defaultApprovalStatus = defaultApprovalStatus;
    }

    public String getApprovedStatus() {
        return approvedStatus;
    }

    public void setApprovedStatus(final String approvedStatus) {
        this.approvedStatus = approvedStatus;
    }

    public String getRejectedStatus() {
        return rejectedStatus;
    }

    public void setRejectedStatus(final String rejectedStatus) {
        this.rejectedStatus = rejectedStatus;
    }
}
