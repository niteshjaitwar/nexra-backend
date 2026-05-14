package com.nexra.hrms.nexra.modules.hrms.recruitment.repository;

import com.nexra.hrms.nexra.modules.hrms.recruitment.entity.CandidateEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CandidateRepository extends JpaRepository<CandidateEntity, String> {
    Optional<CandidateEntity> findByTenantCodeAndCandidateId(String tenantCode, String candidateId);
    List<CandidateEntity> findByTenantCodeOrderByCreatedAtDesc(String tenantCode);
    long countByTenantCode(String tenantCode);
    long countByTenantCodeAndStage(String tenantCode, String stage);

    // Paginated queries
    Page<CandidateEntity> findByTenantCode(String tenantCode, Pageable pageable);
    Page<CandidateEntity> findByTenantCodeAndJobId(String tenantCode, String jobId, Pageable pageable);
    Page<CandidateEntity> findByTenantCodeAndStageIgnoreCase(String tenantCode, String stage, Pageable pageable);
    Page<CandidateEntity> findByTenantCodeAndJobIdAndStageIgnoreCase(String tenantCode, String jobId, String stage, Pageable pageable);
}
