package com.nexra.hrms.nexra.modules.crm.repository;

import com.nexra.hrms.nexra.modules.crm.entity.CrmAccountEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CrmAccountRepository extends JpaRepository<CrmAccountEntity, String> {

    Optional<CrmAccountEntity> findByIdAndTenantCodeIgnoreCase(String id, String tenantCode);

    Page<CrmAccountEntity> findAllByTenantCodeIgnoreCaseOrderByUpdatedAtDescIdDesc(String tenantCode, Pageable pageable);
    Page<CrmAccountEntity> findAllByTenantCodeIgnoreCaseAndOwnerUserIdOrderByUpdatedAtDescIdDesc(
        String tenantCode,
        String ownerUserId,
        Pageable pageable
    );

    long countByTenantCodeIgnoreCase(String tenantCode);
}
