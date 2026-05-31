package com.nexra.hrms.nexra.modules.crm.repository;

import com.nexra.hrms.nexra.modules.crm.entity.CrmContactEntity;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CrmContactRepository extends JpaRepository<CrmContactEntity, String> {

    Optional<CrmContactEntity> findByIdAndTenantCodeIgnoreCase(String id, String tenantCode);

    Page<CrmContactEntity> findAllByTenantCodeIgnoreCaseOrderByUpdatedAtDescIdDesc(String tenantCode, Pageable pageable);

    Page<CrmContactEntity> findAllByTenantCodeIgnoreCaseAndOwnerUserIdOrderByUpdatedAtDescIdDesc(
        String tenantCode,
        String ownerUserId,
        Pageable pageable
    );

    long countByTenantCodeIgnoreCase(String tenantCode);
}
