package com.nexra.hrms.nexra.common.workflow.repository;

import com.nexra.hrms.nexra.common.workflow.entity.WorkflowInstanceEntity;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkflowInstanceRepository extends JpaRepository<WorkflowInstanceEntity, String> {

    Optional<WorkflowInstanceEntity> findByIdAndTenantCodeIgnoreCase(String id, String tenantCode);

    Page<WorkflowInstanceEntity> findAllByTenantCodeIgnoreCaseOrderByCreatedAtDescIdDesc(String tenantCode, Pageable pageable);

    List<WorkflowInstanceEntity> findAllByStatusAndEscalatedFalseAndSlaDueAtBefore(String status, Instant cutoff);
}
