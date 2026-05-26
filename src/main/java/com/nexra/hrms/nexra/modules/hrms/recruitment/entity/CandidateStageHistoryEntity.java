package com.nexra.hrms.nexra.modules.hrms.recruitment.entity;

import com.nexra.hrms.nexra.modules.hrms.recruitment.entity.base.AbstractAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "recruitment_candidate_stage_history")
public class CandidateStageHistoryEntity extends AbstractAuditableEntity {

    @Id
    @Column(name = "history_id", nullable = false, length = 36)
    private String historyId;

    @Column(name = "tenant_code", nullable = false, length = 64)
    private String tenantCode;

    @Column(name = "candidate_id", nullable = false, length = 36)
    private String candidateId;

    @Column(name = "from_stage", length = 30)
    private String fromStage;

    @Column(name = "to_stage", nullable = false, length = 30)
    private String toStage;

    @Column(name = "comment", length = 500)
    private String comment;

    public String getHistoryId() {
        return historyId;
    }

    public void setHistoryId(final String historyId) {
        this.historyId = historyId;
    }

    public String getTenantCode() {
        return tenantCode;
    }

    public void setTenantCode(final String tenantCode) {
        this.tenantCode = tenantCode;
    }

    public String getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(final String candidateId) {
        this.candidateId = candidateId;
    }

    public String getFromStage() {
        return fromStage;
    }

    public void setFromStage(final String fromStage) {
        this.fromStage = fromStage;
    }

    public String getToStage() {
        return toStage;
    }

    public void setToStage(final String toStage) {
        this.toStage = toStage;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(final String comment) {
        this.comment = comment;
    }
}
