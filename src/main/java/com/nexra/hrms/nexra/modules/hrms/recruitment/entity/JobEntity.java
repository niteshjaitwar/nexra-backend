package com.nexra.hrms.nexra.modules.hrms.recruitment.entity;

import com.nexra.hrms.nexra.modules.hrms.recruitment.entity.base.AbstractAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "recruitment_jobs")
public class JobEntity extends AbstractAuditableEntity {

    @Id
    @Column(name = "job_id", nullable = false, length = 36)
    private String jobId;

    @Column(name = "tenant_code", nullable = false, length = 64)
    private String tenantCode;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "department", length = 120)
    private String department;

    @Column(name = "location", length = 120)
    private String location;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    public String getJobId() {
        return jobId;
    }

    public void setJobId(final String jobId) {
        this.jobId = jobId;
    }

    public String getTenantCode() {
        return tenantCode;
    }

    public void setTenantCode(final String tenantCode) {
        this.tenantCode = tenantCode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(final String department) {
        this.department = department;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(final String location) {
        this.location = location;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }
}
