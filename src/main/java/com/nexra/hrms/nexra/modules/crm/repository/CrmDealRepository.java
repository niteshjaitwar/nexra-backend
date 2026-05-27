package com.nexra.hrms.nexra.modules.crm.repository;

import com.nexra.hrms.nexra.modules.crm.entity.CrmDealEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CrmDealRepository extends JpaRepository<CrmDealEntity, String> {

    Optional<CrmDealEntity> findByIdAndTenantCodeIgnoreCase(String id, String tenantCode);

    Page<CrmDealEntity> findAllByTenantCodeIgnoreCaseOrderByUpdatedAtDescIdDesc(String tenantCode, Pageable pageable);
    Page<CrmDealEntity> findAllByTenantCodeIgnoreCaseAndOwnerUserIdOrderByUpdatedAtDescIdDesc(String tenantCode, String ownerUserId, Pageable pageable);

    long countByTenantCodeIgnoreCase(String tenantCode);

    long countByTenantCodeIgnoreCaseAndStageIgnoreCase(String tenantCode, String stage);
}
