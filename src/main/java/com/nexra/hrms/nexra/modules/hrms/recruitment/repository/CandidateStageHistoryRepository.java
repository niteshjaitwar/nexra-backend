package com.nexra.hrms.nexra.modules.hrms.recruitment.repository;

import com.nexra.hrms.nexra.modules.hrms.recruitment.entity.CandidateStageHistoryEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CandidateStageHistoryRepository extends JpaRepository<CandidateStageHistoryEntity, String> {

    List<CandidateStageHistoryEntity> findByTenantCodeAndCandidateIdOrderByCreatedAtDesc(String tenantCode, String candidateId);

    long countByTenantCode(String tenantCode);
}
