package com.nexra.hrms.nexra.modules.hrms.recruitment.repository;

import com.nexra.hrms.nexra.modules.hrms.recruitment.entity.JobEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobRepository extends JpaRepository<JobEntity, String> {
    Optional<JobEntity> findByTenantCodeAndJobId(String tenantCode, String jobId);
    List<JobEntity> findByTenantCodeOrderByCreatedAtDesc(String tenantCode);
    long countByTenantCode(String tenantCode);
    long countByTenantCodeAndStatus(String tenantCode, String status);

    // Paginated queries
    Page<JobEntity> findByTenantCode(String tenantCode, Pageable pageable);
    Page<JobEntity> findByTenantCodeAndStatusIgnoreCase(String tenantCode, String status, Pageable pageable);
}
