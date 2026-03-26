package com.nexra.hrms.nexra.modules.hrms.employee.entity;

import com.nexra.hrms.nexra.modules.hrms.employee.entity.base.AbstractAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "ec_departments")
public class DepartmentEntity extends AbstractAuditableEntity {

    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    @Column(name = "tenant_code", nullable = false, length = 60)
    private String tenantCode;

    @Column(name = "code", nullable = false, length = 60)
    private String code;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "manager_employee_id", length = 36)
    private String managerEmployeeId;

    @Column(name = "active", nullable = false)
    private boolean active;

    public String getId() { return id; }
    public void setId(final String id) { this.id = id; }
    public String getTenantCode() { return tenantCode; }
    public void setTenantCode(final String tenantCode) { this.tenantCode = tenantCode; }
    public String getCode() { return code; }
    public void setCode(final String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(final String name) { this.name = name; }
    public String getManagerEmployeeId() { return managerEmployeeId; }
    public void setManagerEmployeeId(final String managerEmployeeId) { this.managerEmployeeId = managerEmployeeId; }
    public boolean isActive() { return active; }
    public void setActive(final boolean active) { this.active = active; }
}
