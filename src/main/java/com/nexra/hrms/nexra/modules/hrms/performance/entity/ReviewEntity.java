package com.nexra.hrms.nexra.modules.hrms.performance.entity;

import com.nexra.hrms.nexra.modules.hrms.performance.entity.base.AbstractAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "performance_reviews")
public class ReviewEntity extends AbstractAuditableEntity {

    @Id
    @Column(name = "review_id", nullable = false, length = 36)
    private String reviewId;

    @Column(name = "tenant_code", nullable = false, length = 64)
    private String tenantCode;

    @Column(name = "employee_id", nullable = false, length = 36)
    private String employeeId;

    @Column(name = "review_cycle", nullable = false, length = 80)
    private String reviewCycle;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "manager_score", precision = 5, scale = 2)
    private BigDecimal managerScore;

    @Column(name = "employee_comments", length = 2000)
    private String employeeComments;

    @Column(name = "manager_comments", length = 2000)
    private String managerComments;

    public String getReviewId() {
        return reviewId;
    }

    public void setReviewId(final String reviewId) {
        this.reviewId = reviewId;
    }

    public String getTenantCode() {
        return tenantCode;
    }

    public void setTenantCode(final String tenantCode) {
        this.tenantCode = tenantCode;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(final String employeeId) {
        this.employeeId = employeeId;
    }

    public String getReviewCycle() {
        return reviewCycle;
    }

    public void setReviewCycle(final String reviewCycle) {
        this.reviewCycle = reviewCycle;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    public BigDecimal getManagerScore() {
        return managerScore;
    }

    public void setManagerScore(final BigDecimal managerScore) {
        this.managerScore = managerScore;
    }

    public String getEmployeeComments() {
        return employeeComments;
    }

    public void setEmployeeComments(final String employeeComments) {
        this.employeeComments = employeeComments;
    }

    public String getManagerComments() {
        return managerComments;
    }

    public void setManagerComments(final String managerComments) {
        this.managerComments = managerComments;
    }
}
