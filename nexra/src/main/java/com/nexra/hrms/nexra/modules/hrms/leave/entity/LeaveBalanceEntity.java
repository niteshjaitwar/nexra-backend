package com.nexra.hrms.nexra.modules.hrms.leave.entity;

import com.nexra.hrms.nexra.modules.hrms.leave.entity.base.AbstractAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "lv_balances")
public class LeaveBalanceEntity extends AbstractAuditableEntity {
    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;
    @Column(name = "tenant_code", nullable = false, length = 60)
    private String tenantCode;
    @Column(name = "employee_id", nullable = false, length = 36)
    private String employeeId;
    @Column(name = "leave_type_code", nullable = false, length = 40)
    private String leaveTypeCode;
    @Column(name = "opening_balance", nullable = false, precision = 10, scale = 2)
    private BigDecimal openingBalance;
    @Column(name = "accrued_balance", nullable = false, precision = 10, scale = 2)
    private BigDecimal accruedBalance;
    @Column(name = "used_balance", nullable = false, precision = 10, scale = 2)
    private BigDecimal usedBalance;
    @Column(name = "adjusted_balance", nullable = false, precision = 10, scale = 2)
    private BigDecimal adjustedBalance;
    public String getId() { return id; }
    public void setId(final String id) { this.id = id; }
    public String getTenantCode() { return tenantCode; }
    public void setTenantCode(final String tenantCode) { this.tenantCode = tenantCode; }
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(final String employeeId) { this.employeeId = employeeId; }
    public String getLeaveTypeCode() { return leaveTypeCode; }
    public void setLeaveTypeCode(final String leaveTypeCode) { this.leaveTypeCode = leaveTypeCode; }
    public BigDecimal getOpeningBalance() { return openingBalance; }
    public void setOpeningBalance(final BigDecimal openingBalance) { this.openingBalance = openingBalance; }
    public BigDecimal getAccruedBalance() { return accruedBalance; }
    public void setAccruedBalance(final BigDecimal accruedBalance) { this.accruedBalance = accruedBalance; }
    public BigDecimal getUsedBalance() { return usedBalance; }
    public void setUsedBalance(final BigDecimal usedBalance) { this.usedBalance = usedBalance; }
    public BigDecimal getAdjustedBalance() { return adjustedBalance; }
    public void setAdjustedBalance(final BigDecimal adjustedBalance) { this.adjustedBalance = adjustedBalance; }
}

