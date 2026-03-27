package com.nexra.hrms.nexra.modules.hrms.attendance.entity;

import com.nexra.hrms.nexra.modules.hrms.attendance.entity.base.AbstractAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "at_records")
public class AttendanceRecordEntity extends AbstractAuditableEntity {
    @Id @Column(name = "id", nullable = false, length = 36) private String id;
    @Column(name = "tenant_code", nullable = false, length = 60) private String tenantCode;
    @Column(name = "employee_id", nullable = false, length = 36) private String employeeId;
    @Column(name = "work_date", nullable = false) private LocalDate workDate;
    @Column(name = "shift_code", length = 40) private String shiftCode;
    @Column(name = "check_in_at") private Instant checkInAt;
    @Column(name = "check_out_at") private Instant checkOutAt;
    @Column(name = "total_hours", nullable = false, precision = 10, scale = 2) private BigDecimal totalHours;
    @Column(name = "status", nullable = false, length = 30) private String status;
    @Column(name = "notes", length = 500) private String notes;
    public String getId() { return id; } public void setId(final String id) { this.id = id; }
    public String getTenantCode() { return tenantCode; } public void setTenantCode(final String tenantCode) { this.tenantCode = tenantCode; }
    public String getEmployeeId() { return employeeId; } public void setEmployeeId(final String employeeId) { this.employeeId = employeeId; }
    public LocalDate getWorkDate() { return workDate; } public void setWorkDate(final LocalDate workDate) { this.workDate = workDate; }
    public String getShiftCode() { return shiftCode; } public void setShiftCode(final String shiftCode) { this.shiftCode = shiftCode; }
    public Instant getCheckInAt() { return checkInAt; } public void setCheckInAt(final Instant checkInAt) { this.checkInAt = checkInAt; }
    public Instant getCheckOutAt() { return checkOutAt; } public void setCheckOutAt(final Instant checkOutAt) { this.checkOutAt = checkOutAt; }
    public BigDecimal getTotalHours() { return totalHours; } public void setTotalHours(final BigDecimal totalHours) { this.totalHours = totalHours; }
    public String getStatus() { return status; } public void setStatus(final String status) { this.status = status; }
    public String getNotes() { return notes; } public void setNotes(final String notes) { this.notes = notes; }
}

