package com.nexra.hrms.nexra.modules.hrms.leave.entity;

import com.nexra.hrms.nexra.modules.hrms.leave.entity.base.AbstractAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "lv_holidays")
public class HolidayEntity extends AbstractAuditableEntity {
    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;
    @Column(name = "tenant_code", nullable = false, length = 60)
    private String tenantCode;
    @Column(name = "holiday_date", nullable = false)
    private LocalDate holidayDate;
    @Column(name = "name", nullable = false, length = 160)
    private String name;
    @Column(name = "location_code", length = 40)
    private String locationCode;
    @Column(name = "active", nullable = false)
    private boolean active;
    public String getId() { return id; }
    public void setId(final String id) { this.id = id; }
    public String getTenantCode() { return tenantCode; }
    public void setTenantCode(final String tenantCode) { this.tenantCode = tenantCode; }
    public LocalDate getHolidayDate() { return holidayDate; }
    public void setHolidayDate(final LocalDate holidayDate) { this.holidayDate = holidayDate; }
    public String getName() { return name; }
    public void setName(final String name) { this.name = name; }
    public String getLocationCode() { return locationCode; }
    public void setLocationCode(final String locationCode) { this.locationCode = locationCode; }
    public boolean isActive() { return active; }
    public void setActive(final boolean active) { this.active = active; }
}

