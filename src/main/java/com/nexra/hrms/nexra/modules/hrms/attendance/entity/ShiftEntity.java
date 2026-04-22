package com.nexra.hrms.nexra.modules.hrms.attendance.entity;

import com.nexra.hrms.nexra.modules.hrms.attendance.entity.base.AbstractAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "at_shifts")
public class ShiftEntity extends AbstractAuditableEntity {
    @Id @Column(name = "id", nullable = false, length = 36) private String id;
    @Column(name = "tenant_code", nullable = false, length = 60) private String tenantCode;
    @Column(name = "code", nullable = false, length = 40) private String code;
    @Column(name = "name", nullable = false, length = 120) private String name;
    @Column(name = "start_time", nullable = false, length = 8) private String startTime;
    @Column(name = "end_time", nullable = false, length = 8) private String endTime;
    @Column(name = "grace_minutes", nullable = false) private int graceMinutes;
    @Column(name = "active", nullable = false) private boolean active;
    public String getId() { return id; } public void setId(final String id) { this.id = id; }
    public String getTenantCode() { return tenantCode; } public void setTenantCode(final String tenantCode) { this.tenantCode = tenantCode; }
    public String getCode() { return code; } public void setCode(final String code) { this.code = code; }
    public String getName() { return name; } public void setName(final String name) { this.name = name; }
    public String getStartTime() { return startTime; } public void setStartTime(final String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; } public void setEndTime(final String endTime) { this.endTime = endTime; }
    public int getGraceMinutes() { return graceMinutes; } public void setGraceMinutes(final int graceMinutes) { this.graceMinutes = graceMinutes; }
    public boolean isActive() { return active; } public void setActive(final boolean active) { this.active = active; }
}

