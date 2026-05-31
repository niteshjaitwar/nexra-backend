package com.nexra.hrms.nexra.common.security;

public final class NexraPermission {

    public static final String CRM_READ = "crm:read";
    public static final String CRM_WRITE = "crm:write";
    public static final String HRMS_READ = "hrms:read";
    public static final String HRMS_WRITE = "hrms:write";
    public static final String PAYROLL_READ = "payroll:read";
    public static final String PAYROLL_WRITE = "payroll:write";
    public static final String OPS_READ = "ops:read";
    public static final String OPS_WRITE = "ops:write";
    public static final String WORKFLOW_READ = "workflow:read";
    public static final String WORKFLOW_WRITE = "workflow:write";

    private NexraPermission() {
    }
}
