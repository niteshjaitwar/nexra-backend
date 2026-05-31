package com.nexra.hrms.nexra.modules.operations.repository;

import com.nexra.hrms.nexra.modules.operations.entity.OpsProjectEntity;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OpsProjectRepository extends JpaRepository<OpsProjectEntity, String> {

    Optional<OpsProjectEntity> findByIdAndTenantCodeIgnoreCase(String id, String tenantCode);

    Page<OpsProjectEntity> findAllByTenantCodeIgnoreCaseOrderByUpdatedAtDescIdDesc(String tenantCode, Pageable pageable);

    boolean existsByTenantCodeIgnoreCaseAndCodeIgnoreCase(String tenantCode, String code);

    java.util.Optional<OpsProjectEntity> findByTenantCodeIgnoreCaseAndCrmDealId(String tenantCode, String crmDealId);
}
